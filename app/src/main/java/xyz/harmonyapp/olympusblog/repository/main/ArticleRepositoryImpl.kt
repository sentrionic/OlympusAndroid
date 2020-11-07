package xyz.harmonyapp.olympusblog.repository.main

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
import xyz.harmonyapp.olympusblog.persistence.ArticlesDao
import xyz.harmonyapp.olympusblog.persistence.returnOrderedQuery
import xyz.harmonyapp.olympusblog.repository.NetworkBoundResource
import xyz.harmonyapp.olympusblog.repository.buildError
import xyz.harmonyapp.olympusblog.repository.safeApiCall
import xyz.harmonyapp.olympusblog.repository.safeCacheCall
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState.ArticleFields
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState.ViewArticleFields
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

    override fun searchArticles(
        query: String,
        order: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>> {
        var hasMore: Boolean = false
        return object :
            NetworkBoundResource<ArticleListSearchResponse, List<Article>, ArticleViewState>(
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
                val articleList = networkObject.toList()
                hasMore = networkObject.hasMore
                withContext(IO) {
                    for (article in articleList) {
                        try {
                            // Launch each insert as a separate job to be executed in parallel
                            launch {
                                Log.d(TAG, "updateLocalDb: inserting article: ${article}")
                                articlesDao.insert(article)
                            }
                        } catch (e: Exception) {
                            Log.e(
                                TAG,
                                "updateLocalDb: error updating cache data on article post with slug: ${article.slug}. " +
                                        "${e.message}"
                            )
                            // Could send an error report here or something but I don't think you should throw an error to the UI
                            // Since there could be many blog posts being inserted/updated.
                        }
                    }
                }
            }

            override fun handleCacheSuccess(resultObj: List<Article>): DataState<ArticleViewState> {
                val viewState = ArticleViewState(
                    articleFields = ArticleFields(
                        articleList = resultObj,
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
            object : CacheResponseHandler<ArticleViewState, List<Article>>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: List<Article>
                ): DataState<ArticleViewState> {
                    val viewState = ArticleViewState(
                        articleFields = ArticleFields(
                            articleList = resultObj
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


    override fun isAuthorOfArticle(
        id: Int,
        slug: String,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO) {
            mainService.getArticle(
                slug
            )
        }
        emit(
            object : ApiResponseHandler<ArticleViewState, ArticleResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: ArticleResponse): DataState<ArticleViewState> {
                    val viewState = ArticleViewState(
                        viewArticleFields = ViewArticleFields(
                            isAuthorOfArticle = false
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
            }.getResult()
        )
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
                        articlesDao.deleteArticle(article)
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

    override fun updateArticle(
        slug: String,
        title: RequestBody,
        description: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ) = flow {

        val apiResult = safeApiCall(IO) {
            mainService.updateArticle(
                slug,
                title,
                description,
                body,
                image
            )
        }
        emit(
            object : ApiResponseHandler<ArticleViewState, ArticleResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: ArticleResponse): DataState<ArticleViewState> {

                    val updatedArticle = resultObj.toArticle()

                    articlesDao.updateArticle(
                        id = updatedArticle.id,
                        title = updatedArticle.title,
                        description = updatedArticle.description,
                        body = updatedArticle.body,
                        image = updatedArticle.image
                    )

                    return DataState.data(
                        response = Response(
                            message = SUCCESS_ARTICLE_UPDATED,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data = ArticleViewState(
                            viewArticleFields = ViewArticleFields(
                                article = updatedArticle
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

                    val updatedArticle = resultObj.toArticle()

                    articlesDao.updateFavorite(
                        id = updatedArticle.id,
                        favoritesCount = updatedArticle.favoritesCount,
                        favorited = updatedArticle.favorited
                    )

                    return DataState.data(
                        response = Response(
                            message = SUCCESS_TOGGLE_FAVORITE,
                            uiComponentType = UIComponentType.None(),
                            messageType = MessageType.Success()
                        ),
                        data = ArticleViewState(
                            viewArticleFields = ViewArticleFields(
                                article = updatedArticle
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

                    val updatedArticle = resultObj.toArticle()

                    articlesDao.updateBookmark(
                        id = updatedArticle.id,
                        bookmarked = updatedArticle.bookmarked,
                    )

                    return DataState.data(
                        response = Response(
                            message = SUCCESS_TOGGLE_BOOKMARK,
                            uiComponentType = UIComponentType.None(),
                            messageType = MessageType.Success()
                        ),
                        data = ArticleViewState(
                            viewArticleFields = ViewArticleFields(
                                article = updatedArticle
                            )
                        ),
                        stateEvent = stateEvent
                    )

                }

            }.getResult()
        )
    }

}