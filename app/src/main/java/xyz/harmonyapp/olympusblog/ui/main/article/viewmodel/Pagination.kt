package xyz.harmonyapp.olympusblog.ui.main.article.viewmodel

import android.util.Log
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent.ArticleSearchEvent
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent.GetArticlesEvent
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.search.state.SearchStateEvent

fun ArticleViewModel.resetPage() {
    val update = getCurrentViewStateOrNew()
    update.articleFields.page = 1
    setViewState(update)
}

fun ArticleViewModel.refreshFromCache() {
    if (!isJobAlreadyActive(GetArticlesEvent())) {
        setQueryExhausted(false)
        setStateEvent(GetArticlesEvent(false))
    }
}

fun ArticleViewModel.loadFirstPage() {
    if (!isJobAlreadyActive(GetArticlesEvent())) {
        setQueryExhausted(false)
        resetPage()
        setStateEvent(GetArticlesEvent())
        Log.e(
            TAG,
            "ArticleViewModel: loadFirstPage: ${viewState.value!!.articleFields.searchQuery}"
        )
    }
}

fun ArticleViewModel.loadFirstSearchPage() {
    if (!isJobAlreadyActive(ArticleSearchEvent())) {
        setQueryExhausted(false)
        resetPage()
        setStateEvent(ArticleSearchEvent())
    }
}

fun ArticleViewModel.loadFirstFeedPage() {
    if (!isJobAlreadyActive(ArticleStateEvent.ArticleFeedEvent())) {
        setQueryExhausted(false)
        resetPage()
        setStateEvent(ArticleStateEvent.ArticleFeedEvent())
    }
}

fun ArticleViewModel.loadFirstBookmarkPage() {
    if (!isJobAlreadyActive(ArticleStateEvent.ArticleBookmarkEvent())) {
        setQueryExhausted(false)
        resetPage()
        setStateEvent(ArticleStateEvent.ArticleBookmarkEvent())
    }
}

private fun ArticleViewModel.incrementPageNumber() {
    val update = getCurrentViewStateOrNew()
    val page = update.copy().articleFields.page ?: 1
    update.articleFields.page = page.plus(1)
    setViewState(update)
}

fun ArticleViewModel.nextPage() {
    if (!isJobAlreadyActive(GetArticlesEvent())
        && !getIsQueryExhausted()
    ) {
        Log.d(TAG, "ArticleViewModel: Attempting to load next page...")
        incrementPageNumber()
        setStateEvent(GetArticlesEvent())
    }
}

fun ArticleViewModel.handleIncomingArticleListData(viewState: ArticleViewState) {
    viewState.articleFields.let { articleFields ->
        articleFields.articleList?.let { setArticleListData(it) }
    }
}

fun ArticleViewModel.loadProfiles() {
    if (!isJobAlreadyActive(SearchStateEvent.ProfileSearchEvent())) {
        setStateEvent(SearchStateEvent.ProfileSearchEvent())
    }
}

fun ArticleViewModel.loadByTags() {
    if (!isJobAlreadyActive(ArticleStateEvent.ArticlesByTagEvent())) {
        setStateEvent(ArticleStateEvent.ArticlesByTagEvent())
    }
}