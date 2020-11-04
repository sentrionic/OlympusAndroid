package xyz.harmonyapp.olympusblog.ui.main.article.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import okhttp3.MediaType
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.persistence.ArticleQueryUtils
import xyz.harmonyapp.olympusblog.repository.main.ArticleRepository
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.BaseViewModel
import xyz.harmonyapp.olympusblog.ui.DataState
import xyz.harmonyapp.olympusblog.ui.Loading
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent.*
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.utils.AbsentLiveData
import xyz.harmonyapp.olympusblog.utils.PreferenceKeys.Companion.ARTICLE_ORDER
import javax.inject.Inject

class ArticleViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val articleRepository: ArticleRepository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
) : BaseViewModel<ArticleStateEvent, ArticleViewState>() {

    init {
        setArticleOrder(
            sharedPreferences.getString(
                ARTICLE_ORDER,
                ArticleQueryUtils.ARTICLES_DESC
            ).toString()
        )
    }


    override fun handleStateEvent(stateEvent: ArticleStateEvent): LiveData<DataState<ArticleViewState>> {
        return when (stateEvent) {

            is ArticleSearchEvent -> {
                clearLayoutManagerState()
                articleRepository.searchArticles(
                    query = getSearchQuery(),
                    order = getOrder(),
                    page = getPage()
                )
            }

            is RestoreArticleListFromCache -> {
                articleRepository.restoreArticleListFromCache(
                    query = getSearchQuery(),
                    order = getOrder(),
                    page = getPage()
                )
            }

            is CheckAuthorOfArticle -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    authToken.account_id?.let { id ->
                        articleRepository.checkIfAuthor(
                            id = id,
                            slug = getSlug()
                        )
                    }
                } ?: AbsentLiveData.create()
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
                    image = stateEvent.image
                )

            }

            is DeleteArticleEvent -> {
                articleRepository.deleteArticle(
                    article = getArticle()
                )
            }

            is None -> {
                object : LiveData<DataState<ArticleViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(null, Loading(false), null)
                    }
                }
            }
        }
    }

    override fun initNewViewState(): ArticleViewState {
        return ArticleViewState()
    }

    fun cancelActiveJobs() {
        articleRepository.cancelActiveJobs() // cancel active jobs
        handlePendingData() // hide progress bar
    }

    private fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

    fun saveFilterOptions(order: String) {
        editor.putString(ARTICLE_ORDER, order)
        editor.apply()
    }

}