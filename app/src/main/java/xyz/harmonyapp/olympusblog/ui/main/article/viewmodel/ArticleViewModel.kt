package xyz.harmonyapp.olympusblog.ui.main.article.viewmodel

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.persistence.ArticleQueryUtils
import xyz.harmonyapp.olympusblog.repository.main.article.ArticleRepositoryImpl
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.BaseViewModel
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent.*
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.utils.*
import xyz.harmonyapp.olympusblog.utils.ErrorHandling.Companion.INVALID_STATE_EVENT
import xyz.harmonyapp.olympusblog.utils.PreferenceKeys.Companion.ARTICLE_ORDER
import javax.inject.Inject

@MainScope
class ArticleViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val articleRepository: ArticleRepositoryImpl,
    sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
) : BaseViewModel<ArticleViewState>() {

    init {
        setArticleOrder(
            sharedPreferences.getString(
                ARTICLE_ORDER,
                ArticleQueryUtils.ARTICLES_DESC
            ).toString()
        )
    }

    override fun handleNewData(data: ArticleViewState) {

        data.articleFields.let { articleFields ->

            articleFields.articleList?.let { articleList ->
                handleIncomingArticleListData(data)
            }

            articleFields.isQueryExhausted?.let { isQueryExhausted ->
                setQueryExhausted(isQueryExhausted)
            }

        }

        data.viewArticleFields.let { viewArticleFields ->

            viewArticleFields.article?.let { article ->
                setArticle(article)
            }

            viewArticleFields.isAuthorOfArticle?.let { isAuthor ->
                setIsAuthorOfArticle(isAuthor)
            }

            viewArticleFields.commentList?.let { comments ->
                setCommentsList(comments)
            }
        }

        data.updatedArticleFields.let { updatedArticleFields ->

            updatedArticleFields.updatedImageUri?.let { uri ->
                setUpdatedUri(uri)
            }

            updatedArticleFields.updatedArticleTitle?.let { title ->
                setUpdatedTitle(title)
            }

            updatedArticleFields.updatedArticleDescription?.let { description ->
                setUpdatedDescription(description)
            }

            updatedArticleFields.updatedArticleBody?.let { body ->
                setUpdatedBody(body)
            }
        }

        data.viewCommentsFields.let { viewCommentsFields ->
            viewCommentsFields.comment?.let { comment ->
                addComment(comment)
            }
        }
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        if (!isJobAlreadyActive(stateEvent)) {
            val job: Flow<DataState<ArticleViewState>> = when (stateEvent) {

                is ArticleSearchEvent -> {
                    if (stateEvent.clearLayoutManagerState) {
                        clearLayoutManagerState()
                    }
                    articleRepository.searchArticles(
                        stateEvent = stateEvent,
                        query = getSearchQuery(),
                        order = getOrder(),
                        page = getPage()
                    )
                }

                is CleanDBEvent -> {
                    articleRepository.dropDatabase(stateEvent)
                }

                is CheckAuthorOfArticle -> {
                    articleRepository.isAuthorOfArticle(
                        id = getCurrentUserId(),
                        slug = getSlug(),
                        stateEvent = stateEvent
                    )
                }

                is UpdateArticleEvent -> {

                    val title = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.title
                    )

                    val description = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.description
                    )

                    val body = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.body
                    )

                    articleRepository.updateArticle(
                        slug = getSlug(),
                        title = title,
                        description = description,
                        body = body,
                        image = stateEvent.image,
                        stateEvent = stateEvent
                    )

                }

                is DeleteArticleEvent -> {
                    articleRepository.deleteArticle(
                        stateEvent = stateEvent,
                        article = getArticle()
                    )
                }

                is ToggleFavoriteEvent -> {
                    articleRepository.toggleFavorite(
                        stateEvent = stateEvent,
                        article = getArticle()
                    )
                }

                is ToggleBookmarkEvent -> {
                    articleRepository.toggleBookmark(
                        stateEvent = stateEvent,
                        article = getArticle()
                    )
                }

                is GetArticleCommentsEvent -> {
                    articleRepository.getArticleComments(
                        stateEvent = stateEvent,
                        slug = getSlug()
                    )
                }

                is PostCommentEvent -> {
                    articleRepository.postComment(
                        stateEvent = stateEvent,
                        slug = getSlug(),
                        body = stateEvent.body
                    )
                }

                is DeleteCommentEvent -> {
                    articleRepository.deleteComment(
                        stateEvent = stateEvent,
                        slug = getSlug(),
                        id = getComment().id
                    )
                }

                else -> {
                    flow {
                        emit(
                            DataState.error<ArticleViewState>(
                                response = Response(
                                    message = INVALID_STATE_EVENT,
                                    uiComponentType = UIComponentType.None(),
                                    messageType = MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        )
                    }
                }
            }
            launchJob(stateEvent, job)
        }
    }

    override fun initNewViewState(): ArticleViewState {
        return ArticleViewState()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

    fun saveFilterOptions(order: String) {
        editor.putString(ARTICLE_ORDER, order)
        editor.apply()
    }

    fun getCurrentUserId(): Int {
        return sessionManager.cachedToken.value?.account_id?: -1
    }

}