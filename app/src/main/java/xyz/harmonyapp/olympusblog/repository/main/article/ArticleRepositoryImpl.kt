package xyz.harmonyapp.olympusblog.repository.main.article

import android.util.Log
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.api.main.MainService
import xyz.harmonyapp.olympusblog.api.main.responses.ArticleListSearchResponse
import xyz.harmonyapp.olympusblog.api.main.responses.ArticleResponse
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.models.ArticleAuthor
import xyz.harmonyapp.olympusblog.persistence.ArticlesDao
import xyz.harmonyapp.olympusblog.persistence.returnOrderedQuery
import xyz.harmonyapp.olympusblog.repository.NetworkBoundResource
import xyz.harmonyapp.olympusblog.repository.buildError
import xyz.harmonyapp.olympusblog.repository.safeApiCall
import xyz.harmonyapp.olympusblog.repository.safeCacheCall
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState.*
import xyz.harmonyapp.olympusblog.utils.*
import xyz.harmonyapp.olympusblog.utils.ErrorHandling.Companion.ERROR_UNKNOWN
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.SUCCESS_ARTICLE_DELETED
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.SUCCESS_ARTICLE_UPDATED
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.SUCCESS_TOGGLE_BOOKMARK
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.SUCCESS_TOGGLE_FAVORITE
import javax.inject.Inject

@MainScope
class ArticleRepositoryImpl
@Inject
constructor(
    val mainService: MainService,
    val articlesDao: ArticlesDao,
    val sessionManager: SessionManager
) : ArticleRepository {

    private val TAG: String = "AppDebug"

    override fun getArticles(
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>> {
        var hasMore = false
        return object :
            NetworkBoundResource<ArticleListSearchResponse, List<ArticleAuthor>, ArticleViewState>(
                dispatcher = IO,
                stateEvent = stateEvent,
                apiCall = {
                    mainService.searchListArticlePosts(
                        page = page
                    )
                },
                cacheCall = {
                    articlesDao.returnOrderedQuery(
                        query = "",
                        order = "DESC",
                        page = page
                    )
                }
            ) {
            override suspend fun updateCache(networkObject: ArticleListSearchResponse) {
                hasMore = networkObject.hasMore
                withContext(IO) {
                    for (article in networkObject.articles) {
                        try {
                            // Launch each insert as a separate job to be executed in parallel
                            launch {
                                Log.d(TAG, "updateLocalDb: inserting article: ${article}")
                                articlesDao.insert(article.toArticle())
                                articlesDao.insertAuthor(article.author)
                            }
                        } catch (e: Exception) {
                            Log.e(
                                TAG,
                                "updateLocalDb: error updating cache data on article post with slug: ${article.slug}. " +
                                        "${e.message}"
                            )
                        }
                    }
                }
            }

            override fun handleCacheSuccess(resultObj: List<ArticleAuthor>): DataState<ArticleViewState> {

                val articleList: ArrayList<Article> = ArrayList()
                for (articleResponse in resultObj) {
                    articleList.add(
                        articleResponse.toArticle()
                    )
                }

                val viewState = ArticleViewState(
                    articleFields = ArticleFields(
                        articleList = articleList,
                        isQueryExhausted = !hasMore
                    )
                )
                return DataState.data(
                    response = null,
                    data = viewState,
                    stateEvent = stateEvent
                )
            }

        }.result
    }

    override fun searchArticles(
        query: String,
        order: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>> {
        var hasMore = false
        return object :
            NetworkBoundResource<ArticleListSearchResponse, List<ArticleAuthor>, ArticleViewState>(
                dispatcher = IO,
                stateEvent = stateEvent,
                apiCall = {
                    mainService.searchListArticlePosts(
                        query = query,
                        order = order,
                        page = page
                    )
                },
                cacheCall = {
                    articlesDao.returnOrderedQuery(
                        query = query,
                        order = order,
                        page = page
                    )
                }
            ) {
            override suspend fun updateCache(networkObject: ArticleListSearchResponse) {
                Log.d(TAG, "Get Articles: $networkObject")
                hasMore = networkObject.hasMore
                withContext(IO) {
                    for (article in networkObject.articles) {
                        try {
                            // Launch each insert as a separate job to be executed in parallel
                            launch {
                                Log.d(TAG, "updateLocalDb: inserting article: ${article}")
                                articlesDao.insert(article.toArticle())
                                articlesDao.insertAuthor(article.author)
                            }
                        } catch (e: Exception) {
                            Log.e(
                                TAG,
                                "updateLocalDb: error updating cache data on article post with slug: ${article.slug}. " +
                                        "${e.message}"
                            )
                        }
                    }
                }
            }

            override fun handleCacheSuccess(resultObj: List<ArticleAuthor>): DataState<ArticleViewState> {

                val articleList: ArrayList<Article> = ArrayList()
                for (articleResponse in resultObj) {
                    articleList.add(
                        articleResponse.toArticle()
                    )
                }

                val viewState = ArticleViewState(
                    articleFields = ArticleFields(
                        articleList = articleList
                    ),
                    searchFields = SearchFields(
                        isQueryExhausted = !hasMore
                    )
                )
                return DataState.data(
                    response = null,
                    data = viewState,
                    stateEvent = stateEvent
                )
            }

        }.result
    }

    override fun getFeed(
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>> {
        var hasMore = false
        return object :
            NetworkBoundResource<ArticleListSearchResponse, List<ArticleAuthor>, ArticleViewState>(
                dispatcher = IO,
                stateEvent = stateEvent,
                apiCall = {
                    mainService.getFeed(
                        page = page
                    )
                },
                cacheCall = {
                    articlesDao.getFeed(
                        page = page,
                        authorIds = articlesDao.getFollowedAuthors(),
                    )
                }
            ) {
            override suspend fun updateCache(networkObject: ArticleListSearchResponse) {
                hasMore = networkObject.hasMore
                withContext(IO) {
                    for (article in networkObject.articles) {
                        try {
                            // Launch each insert as a separate job to be executed in parallel
                            launch {
                                Log.d(TAG, "updateLocalDb: inserting article: ${article}")
                                articlesDao.insert(article.toArticle())
                                article.author.following = true
                                articlesDao.insertAuthor(article.author)
                            }
                        } catch (e: Exception) {
                            Log.e(
                                TAG,
                                "updateLocalDb: error updating cache data on article post with slug: ${article.slug}. " +
                                        "${e.message}"
                            )
                        }
                    }
                }
            }

            override fun handleCacheSuccess(resultObj: List<ArticleAuthor>): DataState<ArticleViewState> {

                val articleList: ArrayList<Article> = ArrayList()
                for (articleResponse in resultObj) {
                    articleList.add(
                        articleResponse.toArticle()
                    )
                }

                val viewState = ArticleViewState(
                    articleFields = ArticleFields(
                        articleList = articleList,
                        isQueryExhausted = !hasMore
                    )
                )
                return DataState.data(
                    response = null,
                    data = viewState,
                    stateEvent = stateEvent
                )
            }

        }.result
    }

    override fun getBookmarkedArticles(
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>> {
        var hasMore = false
        return object :
            NetworkBoundResource<ArticleListSearchResponse, List<ArticleAuthor>, ArticleViewState>(
                dispatcher = IO,
                stateEvent = stateEvent,
                apiCall = {
                    mainService.getBookmarked(
                        page = page
                    )
                },
                cacheCall = {
                    articlesDao.getBookmarkedArticles(
                        page = page
                    )
                }
            ) {
            override suspend fun updateCache(networkObject: ArticleListSearchResponse) {
                hasMore = networkObject.hasMore
                withContext(IO) {
                    for (article in networkObject.articles) {
                        try {
                            // Launch each insert as a separate job to be executed in parallel
                            launch {
                                Log.d(TAG, "updateLocalDb: inserting article: ${article}")
                                articlesDao.insert(article.toArticle())
                                articlesDao.insertAuthor(article.author)
                            }
                        } catch (e: Exception) {
                            Log.e(
                                TAG,
                                "updateLocalDb: error updating cache data on article post with slug: ${article.slug}. " +
                                        "${e.message}"
                            )
                        }
                    }
                }
            }

            override fun handleCacheSuccess(resultObj: List<ArticleAuthor>): DataState<ArticleViewState> {

                val articleList: ArrayList<Article> = ArrayList()
                for (articleResponse in resultObj) {
                    articleList.add(
                        articleResponse.toArticle()
                    )
                }

                val viewState = ArticleViewState(
                    articleFields = ArticleFields(
                        articleList = articleList,
                        isQueryExhausted = !hasMore
                    )
                )
                return DataState.data(
                    response = null,
                    data = viewState,
                    stateEvent = stateEvent
                )
            }

        }.result
    }

    override fun getArticlesByTag(
        page: Int,
        query: String,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>> {
        var hasMore = false
        return object :
            NetworkBoundResource<ArticleListSearchResponse, List<ArticleAuthor>, ArticleViewState>(
                dispatcher = IO,
                stateEvent = stateEvent,
                apiCall = {
                    mainService.searchListArticlePosts(
                        tag = query,
                        page = page
                    )
                },
                cacheCall = {
                    articlesDao.getArticlesByTag(
                        query = query,
                        page = page
                    )
                }
            ) {
            override suspend fun updateCache(networkObject: ArticleListSearchResponse) {
                hasMore = networkObject.hasMore
                withContext(IO) {
                    for (article in networkObject.articles) {
                        try {
                            // Launch each insert as a separate job to be executed in parallel
                            launch {
                                Log.d(TAG, "updateLocalDb: inserting article: ${article}")
                                articlesDao.insert(article.toArticle())
                                articlesDao.insertAuthor(article.author)
                            }
                        } catch (e: Exception) {
                            Log.e(
                                TAG,
                                "updateLocalDb: error updating cache data on article post with slug: ${article.slug}. " +
                                        "${e.message}"
                            )
                        }
                    }
                }
            }

            override fun handleCacheSuccess(resultObj: List<ArticleAuthor>): DataState<ArticleViewState> {

                val articleList: ArrayList<Article> = ArrayList()
                for (articleResponse in resultObj) {
                    articleList.add(
                        articleResponse.toArticle()
                    )
                }

                val viewState = ArticleViewState(
                    articleFields = ArticleFields(
                        articleList = articleList,
                        isQueryExhausted = !hasMore
                    )
                )
                return DataState.data(
                    response = null,
                    data = viewState,
                    stateEvent = stateEvent
                )
            }

        }.result
    }

    override fun restoreArticleListFromCache(
        query: String,
        order: String,
        page: Int,
        stateEvent: StateEvent
    ) = flow {

        val cacheResult = safeCacheCall(IO) {
            articlesDao.returnOrderedQuery(
                query = query,
                order = order,
                page = page
            )
        }
        emit(
            object : CacheResponseHandler<ArticleViewState, List<ArticleAuthor>>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: List<ArticleAuthor>
                ): DataState<ArticleViewState> {
                    val articleList: ArrayList<Article> = ArrayList()
                    for (articleResponse in resultObj) {
                        articleList.add(
                            articleResponse.toArticle()
                        )
                    }
                    val viewState = ArticleViewState(
                        articleFields = ArticleFields(
                            articleList = articleList
                        )
                    )
                    return DataState.data(
                        response = null,
                        data = viewState,
                        stateEvent = stateEvent
                    )
                }

            }.getResult()
        )
    }

    override fun dropDatabase(stateEvent: StateEvent) = flow {
        val cacheResult = safeCacheCall(IO) {
            articlesDao.dropArticlesTable()
            articlesDao.dropAuthorsTable()
        }
        emit(
            object : CacheResponseHandler<ArticleViewState, Unit>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: Unit
                ): DataState<ArticleViewState> {
                    return DataState.data(
                        response = null,
                        data = null,
                        stateEvent = stateEvent
                    )
                }

            }.getResult()
        )
    }

    override fun isAuthorOfArticle(
        id: Int,
        slug: String,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>> {
        return object :
            NetworkBoundResource<ArticleResponse, ArticleAuthor, ArticleViewState>(
                dispatcher = IO,
                stateEvent = stateEvent,
                apiCall = {
                    mainService.getArticle(
                        slug
                    )
                },
                cacheCall = {
                    articlesDao.getArticleBySlug(
                        slug = slug
                    )
                }
            ) {
            override suspend fun updateCache(networkObject: ArticleResponse) {
                withContext(IO) {
                    try {
                        // Launch each insert as a separate job to be executed in parallel
                        launch {
                            Log.d(TAG, "updateLocalDb: inserting article: $networkObject")
                            articlesDao.insertAuthor(networkObject.author)
                        }
                    } catch (e: Exception) {
                        Log.e(
                            TAG,
                            "updateLocalDb: error updating cache data on article post with slug: ${slug}. " +
                                    "${e.message}"
                        )
                    }
                }
            }

            override fun handleCacheSuccess(resultObj: ArticleAuthor): DataState<ArticleViewState> {

                val article = resultObj.toArticle()

                val viewState = ArticleViewState(
                    viewArticleFields = ViewArticleFields(
                        isAuthorOfArticle = false,
                        article = article
                    ),
                    viewProfileFields = ViewProfileFields(
                        profile = article.author
                    )
                )
                return when {
                    resultObj.author.id != id -> {
                        DataState.data(
                            response = null,
                            data = viewState,
                            stateEvent = stateEvent
                        )
                    }

                    resultObj.author.id == id -> {
                        viewState.viewArticleFields.isAuthorOfArticle = true
                        DataState.data(
                            response = null,
                            data = viewState,
                            stateEvent = stateEvent
                        )
                    }

                    else -> {
                        buildError(
                            ERROR_UNKNOWN,
                            UIComponentType.None(),
                            stateEvent
                        )
                    }
                }
            }

        }.result
    }

    override fun deleteArticle(
        article: Article,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO) {
            mainService.deleteArticle(
                article.slug
            )
        }
        emit(
            object : ApiResponseHandler<ArticleViewState, ArticleResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: ArticleResponse): DataState<ArticleViewState> {

                    if (resultObj.id == article.id) {
                        articlesDao.deleteArticle(resultObj.toArticle())
                        return DataState.data(
                            response = Response(
                                message = SUCCESS_ARTICLE_DELETED,
                                uiComponentType = UIComponentType.Toast(),
                                messageType = MessageType.Success()
                            ),
                            stateEvent = stateEvent
                        )
                    } else {
                        return buildError(
                            ERROR_UNKNOWN,
                            UIComponentType.Dialog(),
                            stateEvent
                        )
                    }
                }
            }.getResult()
        )
    }

    override fun createNewArticle(
        title: RequestBody,
        description: RequestBody,
        body: RequestBody,
        tagList: List<String>,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ) = flow {

        val apiResult = safeApiCall(IO) {
            mainService.createArticle(
                title = title,
                description = description,
                body = body,
                tagList = tagList,
                image = image
            )
        }

        emit(
            object : ApiResponseHandler<ArticleViewState, ArticleResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: ArticleResponse): DataState<ArticleViewState> {

                    articlesDao.insert(resultObj.toArticle())
                    val article = articlesDao.getArticleBySlug(resultObj.slug)

                    return DataState.data(
                        response = Response(
                            message = SuccessHandling.SUCCESS_ARTICLE_CREATED,
                            uiComponentType = UIComponentType.Dialog(),
                            messageType = MessageType.Success()
                        ),
                        data = ArticleViewState(
                            newArticleFields = NewArticleFields(
                                newArticle = article.toArticle()
                            )
                        ),
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

    override fun updateArticle(
        slug: String,
        title: RequestBody,
        description: RequestBody,
        body: RequestBody,
        tags: List<String>,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ) = flow {

        val apiResult = safeApiCall(IO) {
            mainService.updateArticle(
                slug,
                title,
                description,
                body,
                tags,
                image
            )
        }
        emit(
            object : ApiResponseHandler<ArticleViewState, ArticleResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: ArticleResponse): DataState<ArticleViewState> {
                    val article = resultObj.toArticle()
                    articlesDao.updateArticle(
                        id = article.id,
                        title = article.title,
                        description = article.description,
                        body = article.description,
                        image = article.image,
                        tags = article.tagList.replace("\"", "")
                    )
                    val updatedArticle = articlesDao.getArticleBySlug(resultObj.slug)
                    return DataState.data(
                        response = Response(
                            message = SUCCESS_ARTICLE_UPDATED,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data = ArticleViewState(
                            viewArticleFields = ViewArticleFields(
                                article = updatedArticle.toArticle()
                            )
                        ),
                        stateEvent = stateEvent
                    )

                }

            }.getResult()
        )
    }

    override fun toggleFavorite(
        article: Article,
        stateEvent: StateEvent
    ) = flow {

        val apiResult = safeApiCall(IO) {
            when {
                article.favorited -> {
                    mainService.unfavoriteArticle(
                        slug = article.slug,
                    )
                }
                else -> {
                    mainService.favoriteArticle(slug = article.slug)
                }
            }
        }
        emit(
            object : ApiResponseHandler<ArticleViewState, ArticleResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: ArticleResponse): DataState<ArticleViewState> {

                    articlesDao.updateFavorite(
                        id = resultObj.id,
                        favoritesCount = resultObj.favoritesCount,
                        favorited = resultObj.favorited
                    )

                    val updatedArticle = articlesDao.getArticleBySlug(resultObj.slug)

                    return DataState.data(
                        response = Response(
                            message = SUCCESS_TOGGLE_FAVORITE,
                            uiComponentType = UIComponentType.None(),
                            messageType = MessageType.Success()
                        ),
                        data = ArticleViewState(
                            viewArticleFields = ViewArticleFields(
                                article = updatedArticle.toArticle()
                            )
                        ),
                        stateEvent = stateEvent
                    )

                }

            }.getResult()
        )
    }

    override fun toggleBookmark(
        article: Article,
        stateEvent: StateEvent
    ) = flow {

        val apiResult = safeApiCall(IO) {
            when {
                article.bookmarked -> {
                    mainService.unbookmarkArticle(
                        slug = article.slug,
                    )
                }
                else -> {
                    mainService.bookmarkArticle(slug = article.slug)
                }
            }
        }
        emit(
            object : ApiResponseHandler<ArticleViewState, ArticleResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: ArticleResponse): DataState<ArticleViewState> {

                    articlesDao.updateBookmark(
                        id = resultObj.id,
                        bookmarked = resultObj.bookmarked,
                    )

                    val updatedArticle = articlesDao.getArticleBySlug(resultObj.slug)

                    return DataState.data(
                        response = Response(
                            message = SUCCESS_TOGGLE_BOOKMARK,
                            uiComponentType = UIComponentType.None(),
                            messageType = MessageType.Success()
                        ),
                        data = ArticleViewState(
                            viewArticleFields = ViewArticleFields(
                                article = updatedArticle.toArticle()
                            )
                        ),
                        stateEvent = stateEvent
                    )

                }

            }.getResult()
        )
    }
}