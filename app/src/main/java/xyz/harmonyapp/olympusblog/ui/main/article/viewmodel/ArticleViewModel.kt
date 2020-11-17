package xyz.harmonyapp.olympusblog.ui.main.article.viewmodel

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.persistence.ArticleQueryUtils
import xyz.harmonyapp.olympusblog.repository.main.article.ArticleRepositoryImpl
import xyz.harmonyapp.olympusblog.repository.main.comment.CommentRepositoryImpl
import xyz.harmonyapp.olympusblog.repository.main.profile.ProfileRepositoryImpl
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.BaseViewModel
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent.*
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.search.state.SearchStateEvent
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
    private val profileRepository: ProfileRepositoryImpl,
    private val commentRepository: CommentRepositoryImpl,
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

            updatedArticleFields.updatedArticleTags?.let { tags ->
                setUpdatedTags(tags)
            }
        }

        data.newArticleFields.let { newArticleFields ->
            setNewArticleFields(
                newArticleFields.newArticleTitle,
                newArticleFields.newArticleDescription,
                newArticleFields.newArticleBody,
                newArticleFields.newArticleTags,
                newArticleFields.newImageUri
            )
            newArticleFields.newArticle?.let { newArticle ->
                addNewArticle(newArticle)
            }
        }

        data.viewCommentsFields.let { viewCommentsFields ->
            viewCommentsFields.comment?.let { comment ->
                addComment(comment)
            }
        }

        data.searchFields.let { searchFields ->

            searchFields.profileList?.let { profileList ->
                setProfileListData(profileList)
            }

            searchFields.isQueryExhausted?.let { isQueryExhausted ->
                setQueryExhausted(isQueryExhausted)
            }
        }

        data.viewProfileFields.let { viewProfileFields ->
            viewProfileFields.profile?.let { profile ->
                setProfile(profile)
            }

            viewProfileFields.articleList?.let { list ->
                val update = getCurrentViewStateOrNew()
                update.viewProfileFields.articleList = list
                setViewState(update)
            }
        }
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        if (!isJobAlreadyActive(stateEvent)) {
            val job: Flow<DataState<ArticleViewState>> = when (stateEvent) {

                is GetArticlesEvent -> {
                    if (stateEvent.clearLayoutManagerState) {
                        clearLayoutManagerState()
                    }
                    articleRepository.getArticles(
                        stateEvent = stateEvent,
                        page = getPage()
                    )
                }

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

                is ArticleFeedEvent -> {
                    if (stateEvent.clearLayoutManagerState) {
                        clearLayoutManagerState()
                    }
                    articleRepository.getFeed(
                        stateEvent = stateEvent,
                        page = getPage()
                    )
                }

                is ArticleBookmarkEvent -> {
                    if (stateEvent.clearLayoutManagerState) {
                        clearLayoutManagerState()
                    }
                    articleRepository.getBookmarkedArticles(
                        stateEvent = stateEvent,
                        page = getPage()
                    )
                }

                is ArticlesByTagEvent -> {
                    if (stateEvent.clearLayoutManagerState) {
                        clearLayoutManagerState()
                    }
                    articleRepository.getArticlesByTag(
                        stateEvent = stateEvent,
                        page = getPage(),
                        query = getSearchQuery()
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

                is CreateNewArticleEvent -> {
                    val title = stateEvent.title.toRequestBody("text/plain".toMediaTypeOrNull())
                    val description =
                        stateEvent.description.toRequestBody("text/plain".toMediaTypeOrNull())
                    val body = stateEvent.body.toRequestBody("text/plain".toMediaTypeOrNull())
                    val tags = stateEvent.tags.split(",")

                    articleRepository.createNewArticle(
                        title,
                        description,
                        body,
                        tags,
                        stateEvent.image,
                        stateEvent = stateEvent
                    )
                }

                is UpdateArticleEvent -> {

                    val title = stateEvent.title
                        .toRequestBody("text/plain".toMediaTypeOrNull())

                    val description = stateEvent.description
                        .toRequestBody("text/plain".toMediaTypeOrNull())

                    val body = stateEvent.body
                        .toRequestBody("text/plain".toMediaTypeOrNull())

                    val tags = stateEvent.tags.replace(" ", "").split(",")

                    articleRepository.updateArticle(
                        slug = getSlug(),
                        title = title,
                        description = description,
                        body = body,
                        tags = tags,
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
                    commentRepository.getArticleComments(
                        stateEvent = stateEvent,
                        slug = getSlug()
                    )
                }

                is PostCommentEvent -> {
                    commentRepository.postComment(
                        stateEvent = stateEvent,
                        slug = getSlug(),
                        body = stateEvent.body
                    )
                }

                is DeleteCommentEvent -> {
                    commentRepository.deleteComment(
                        stateEvent = stateEvent,
                        slug = getSlug(),
                        id = getComment().id
                    )
                }

                is SearchStateEvent.ProfileSearchEvent -> {
                    profileRepository.searchProfiles(
                        query = getSearchQuery(),
                        stateEvent = stateEvent
                    )
                }

                is SearchStateEvent.ToggleFollowEvent -> {
                    profileRepository.toggleFollow(author = getAuthor(), stateEvent = stateEvent)
                }

                is SearchStateEvent.GetAuthorArticlesEvent -> {
                    profileRepository.getAuthorStories(
                        author = getAuthor(),
                        stateEvent = stateEvent
                    )
                }

                is SearchStateEvent.GetAuthorFavoritesEvent -> {
                    profileRepository.getAuthorFavorites(
                        author = getAuthor(),
                        stateEvent = stateEvent
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
        return sessionManager.cachedToken.value?.account_id ?: -1
    }

}