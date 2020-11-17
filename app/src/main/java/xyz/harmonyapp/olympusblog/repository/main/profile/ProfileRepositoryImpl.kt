package xyz.harmonyapp.olympusblog.repository.main.profile

import android.util.Log
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import xyz.harmonyapp.olympusblog.api.main.MainService
import xyz.harmonyapp.olympusblog.api.main.responses.ArticleListSearchResponse
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.models.Author
import xyz.harmonyapp.olympusblog.repository.safeApiCall
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState.SearchFields
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState.ViewProfileFields
import xyz.harmonyapp.olympusblog.utils.ApiResponseHandler
import xyz.harmonyapp.olympusblog.utils.DataState
import xyz.harmonyapp.olympusblog.utils.StateEvent
import javax.inject.Inject

@MainScope
class ProfileRepositoryImpl
@Inject
constructor(
    val mainService: MainService,
    val sessionManager: SessionManager
) : ProfileRepository {

    private val TAG: String = "AppDebug"

    override fun searchProfiles(
        query: String,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO) {
            mainService.searchProfiles(query)
        }
        emit(
            object : ApiResponseHandler<ArticleViewState, List<Author>>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: List<Author>
                ): DataState<ArticleViewState> {

                    Log.d(TAG, "Profiles: $resultObj")

                    return DataState.data(
                        response = null,
                        data = ArticleViewState(
                            searchFields = SearchFields(
                                profileList = resultObj
                            )
                        ),
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

    override fun toggleFollow(
        author: Author,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO) {
            if (author.following) {
                mainService.unfollowUser(author.username)
            } else {
                mainService.followUser(author.username)
            }
        }
        emit(
            object : ApiResponseHandler<ArticleViewState, Author>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: Author
                ): DataState<ArticleViewState> {

                    return DataState.data(
                        response = null,
                        data = ArticleViewState(
                            viewProfileFields = ViewProfileFields(
                                profile = resultObj
                            )
                        ),
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

    override fun getAuthorStories(
        author: Author,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO) {
            mainService.searchListArticlePosts(
                author = author.username,
            )
        }
        emit(
            object : ApiResponseHandler<ArticleViewState, ArticleListSearchResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: ArticleListSearchResponse
                ): DataState<ArticleViewState> {

                    val list = mutableListOf<Article>()
                    resultObj.articles.forEach { article ->
                        list.add(
                            Article(
                                id = article.id,
                                title = article.title,
                                description = article.description,
                                slug = article.slug,
                                body = article.body,
                                image = article.image,
                                favoritesCount = article.favoritesCount,
                                createdAt = article.createdAt,
                                favorited = article.favorited,
                                bookmarked = article.bookmarked,
                                tagList = article.tagList,
                                author = article.author
                            )
                        )
                    }

                    return DataState.data(
                        response = null,
                        data = ArticleViewState(
                            viewProfileFields = ViewProfileFields(
                                articleList = list
                            )
                        ),
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

    override fun getAuthorFavorites(
        author: Author,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO) {
            mainService.searchListArticlePosts(
                favorited = author.username,
            )
        }
        emit(
            object : ApiResponseHandler<ArticleViewState, ArticleListSearchResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: ArticleListSearchResponse
                ): DataState<ArticleViewState> {

                    val list = mutableListOf<Article>()
                    resultObj.articles.forEach { article ->
                        list.add(
                            Article(
                                id = article.id,
                                title = article.title,
                                description = article.description,
                                slug = article.slug,
                                body = article.body,
                                image = article.image,
                                favoritesCount = article.favoritesCount,
                                createdAt = article.createdAt,
                                favorited = article.favorited,
                                bookmarked = article.bookmarked,
                                tagList = article.tagList,
                                author = article.author
                            )
                        )
                    }

                    return DataState.data(
                        response = null,
                        data = ArticleViewState(
                            viewProfileFields = ViewProfileFields(
                                articleList = list
                            )
                        ),
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }
}
