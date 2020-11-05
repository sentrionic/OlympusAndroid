package xyz.harmonyapp.olympusblog.repository.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.api.main.MainService
import xyz.harmonyapp.olympusblog.api.main.responses.ArticleListSearchResponse
import xyz.harmonyapp.olympusblog.api.main.responses.ArticleResponse
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.persistence.ArticlesDao
import xyz.harmonyapp.olympusblog.persistence.returnOrderedQuery
import xyz.harmonyapp.olympusblog.repository.JobManager
import xyz.harmonyapp.olympusblog.repository.NetworkBoundResource
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.DataState
import xyz.harmonyapp.olympusblog.ui.Response
import xyz.harmonyapp.olympusblog.ui.ResponseType
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState.ArticleFields
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState.ViewArticleFields
import xyz.harmonyapp.olympusblog.utils.AbsentLiveData
import xyz.harmonyapp.olympusblog.utils.ApiSuccessResponse
import xyz.harmonyapp.olympusblog.utils.Constants.Companion.PAGINATION_PAGE_SIZE
import xyz.harmonyapp.olympusblog.utils.ErrorHandling.Companion.ERROR_UNKNOWN
import xyz.harmonyapp.olympusblog.utils.GenericApiResponse
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.SUCCESS_ARTICLE_DELETED
import javax.inject.Inject

@MainScope
class ArticleRepository
@Inject
constructor(
    val mainService: MainService,
    val articlesDao: ArticlesDao,
    val sessionManager: SessionManager
) : JobManager("ArticleRepository") {

    private val TAG: String = "AppDebug"

    fun searchArticles(
        query: String,
        order: String,
        page: Int
    ): LiveData<DataState<ArticleViewState>> {
        return object :
            NetworkBoundResource<ArticleListSearchResponse, List<Article>, ArticleViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                false,
                true
            ) {
            // if network is down, view cache only and return
            override suspend fun createCacheRequestAndReturn() {
                withContext(Dispatchers.Main) {

                    // finishing by viewing db cache
                    result.addSource(loadFromCache()) { viewState ->
                        viewState.articleFields.isQueryInProgress = false
                        if (page * PAGINATION_PAGE_SIZE > viewState.articleFields.articleList.size) {
                            viewState.articleFields.isQueryExhausted = true
                        }
                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<ArticleListSearchResponse>
            ) {

                val articles: ArrayList<Article> = ArrayList()
                for (articleResponse in response.body.articles) {
                    articles.add(
                        articleResponse.toArticle()
                    )
                }

                updateLocalDb(articles)

                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<ArticleListSearchResponse>> {
                return mainService.searchListArticlePosts(
                    query = query,
                    page = page,
                    order = order
                )
            }

            override fun loadFromCache(): LiveData<ArticleViewState> {
                return articlesDao.returnOrderedQuery(query = query, page = page, order = order)
                    .switchMap {
                        object : LiveData<ArticleViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = ArticleViewState(
                                    ArticleFields(
                                        articleList = it,
                                        isQueryInProgress = true
                                    )
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: List<Article>?) {
                // loop through list and update the local db
                if (cacheObject != null) {
                    withContext(IO) {
                        for (articlePost in cacheObject) {
                            try {
                                // Launch each insert as a separate job to be executed in parallel
                                launch {
                                    Log.d(TAG, "updateLocalDb: inserting article: ${articlePost}")
                                    articlesDao.insert(articlePost)
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    TAG,
                                    "updateLocalDb: error updating cache data on article post with slug: ${articlePost.slug}. " +
                                            "${e.message}"
                                )
                                // Could send an error report here or something but I don't think you should throw an error to the UI
                                // Since there could be many article posts being inserted/updated.
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "updateLocalDb: article post list is null")
                }
            }

            override fun setJob(job: Job) {
                addJob("searchArticlePosts", job)
            }

        }.asLiveData()
    }

    fun restoreArticleListFromCache(
        query: String,
        order: String,
        page: Int
    ): LiveData<DataState<ArticleViewState>> {
        return object :
            NetworkBoundResource<ArticleListSearchResponse, List<Article>, ArticleViewState>(
                sessionManager.isConnectedToTheInternet(),
                false,
                false,
                true
            ) {
            override suspend fun createCacheRequestAndReturn() {
                withContext(Dispatchers.Main) {
                    result.addSource(loadFromCache()) { viewState ->
                        viewState.articleFields.isQueryInProgress = false
                        if (page * PAGINATION_PAGE_SIZE > viewState.articleFields.articleList.size) {
                            viewState.articleFields.isQueryExhausted = true
                        }
                        onCompleteJob(
                            DataState.data(
                                viewState,
                                null
                            )
                        )
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<ArticleListSearchResponse>
            ) {
                // ignore
            }

            override fun createCall(): LiveData<GenericApiResponse<ArticleListSearchResponse>> {
                return AbsentLiveData.create()
            }

            override fun loadFromCache(): LiveData<ArticleViewState> {
                return articlesDao.returnOrderedQuery(query = query, page = page, order = order)
                    .switchMap {
                        object : LiveData<ArticleViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = ArticleViewState(
                                    ArticleFields(
                                        articleList = it,
                                        isQueryInProgress = true
                                    )
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: List<Article>?) {
                // ignore
            }

            override fun setJob(job: Job) {
                addJob("restoreBlogListFromCache", job)
            }

        }.asLiveData()
    }


    fun checkIfAuthor(
        id: Int,
        slug: String
    ): LiveData<DataState<ArticleViewState>> {
        return object : NetworkBoundResource<ArticleResponse, Any, ArticleViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {


            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<ArticleResponse>) {
                withContext(Dispatchers.Main) {

                    Log.d(TAG, "handleApiSuccessResponse: ${response.body}")
                    when {
                        response.body.author.id != id -> {
                            onCompleteJob(
                                DataState.data(
                                    data = ArticleViewState(
                                        viewArticleFields = ViewArticleFields(
                                            isAuthorOfArticle = false
                                        )
                                    ),
                                    response = null
                                )
                            )
                        }
                        response.body.author.id == id -> {
                            onCompleteJob(
                                DataState.data(
                                    data = ArticleViewState(
                                        viewArticleFields = ViewArticleFields(
                                            isAuthorOfArticle = true
                                        )
                                    ),
                                    response = null
                                )
                            )
                        }
                        else -> {
                            onErrorReturn(
                                ERROR_UNKNOWN,
                                shouldUseDialog = false,
                                shouldUseToast = false
                            )
                        }
                    }
                }
            }

            // not applicable
            override fun loadFromCache(): LiveData<ArticleViewState> {
                return AbsentLiveData.create()
            }

            // Make an update and change nothing.
            // If they are not the author it will return: "You don't have permission to edit that."
            override fun createCall(): LiveData<GenericApiResponse<ArticleResponse>> {
                return mainService.getArticle(
                    slug
                )
            }

            // not applicable
            override suspend fun updateLocalDb(cacheObject: Any?) {

            }

            override fun setJob(job: Job) {
                addJob("isAuthorOfBlogPost", job)
            }
        }.asLiveData()
    }

    fun deleteArticle(
        article: Article
    ): LiveData<DataState<ArticleViewState>> {
        return object : NetworkBoundResource<ArticleResponse, Article, ArticleViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<ArticleResponse>) {
                updateLocalDb(response.body.toArticle())
            }

            // not applicable
            override fun loadFromCache(): LiveData<ArticleViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<ArticleResponse>> {
                return mainService.deleteArticle(
                    article.slug
                )
            }

            override suspend fun updateLocalDb(cacheObject: Article?) {
                cacheObject?.let { article ->
                    articlesDao.deleteArticle(article)
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(SUCCESS_ARTICLE_DELETED, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("deleteArticle", job)
            }

        }.asLiveData()
    }

    fun updateArticle(
        slug: String,
        title: RequestBody,
        description: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<ArticleViewState>> {
        return object : NetworkBoundResource<ArticleResponse, Article, ArticleViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<ArticleResponse>
            ) {

                updateLocalDb(response.body.toArticle())

                withContext(Dispatchers.Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            ArticleViewState(
                                viewArticleFields = ViewArticleFields(
                                    article = response.body.toArticle()
                                )
                            ),
                            Response("Successfully updated your article", ResponseType.Toast())
                        )
                    )
                }
            }

            // not applicable
            override fun loadFromCache(): LiveData<ArticleViewState> {
                return AbsentLiveData.create()
            }

            override fun createCall(): LiveData<GenericApiResponse<ArticleResponse>> {
                return mainService.updateArticle(
                    slug,
                    title,
                    description,
                    body,
                    image
                )
            }

            override suspend fun updateLocalDb(cacheObject: Article?) {
                cacheObject?.let { article ->
                    articlesDao.updateArticle(
                        article.id,
                        article.title,
                        article.body,
                        article.description,
                        article.image
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("updateBlogPost", job)
            }

        }.asLiveData()
    }

}