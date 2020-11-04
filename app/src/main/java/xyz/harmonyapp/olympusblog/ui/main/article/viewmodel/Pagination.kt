package xyz.harmonyapp.olympusblog.ui.main.article.viewmodel

import android.util.Log
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent.ArticleSearchEvent
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent.RestoreArticleListFromCache
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState

fun ArticleViewModel.resetPage() {
    val update = getCurrentViewStateOrNew()
    update.articleFields.page = 1
    setViewState(update)
}

fun ArticleViewModel.refreshFromCache() {
    setQueryInProgress(true)
    setQueryExhausted(false)
    setStateEvent(RestoreArticleListFromCache())
}

fun ArticleViewModel.loadFirstPage() {
    setQueryInProgress(true)
    setQueryExhausted(false)
    resetPage()
    setStateEvent(ArticleSearchEvent())
    Log.e(TAG, "ArticleViewModel: loadFirstPage: ${getSearchQuery()}")
}

private fun ArticleViewModel.incrementPageNumber() {
    val update = getCurrentViewStateOrNew()
    val page = update.copy().articleFields.page // get current page
    update.articleFields.page = page + 1
    setViewState(update)
}

fun ArticleViewModel.nextPage() {
    if (!getIsQueryInProgress()
        && !getIsQueryExhausted()
    ) {
        Log.d(TAG, "ArticleViewModel: Attempting to load next page...")
        incrementPageNumber()
        setQueryInProgress(true)
        setStateEvent(ArticleSearchEvent())
    }
}

fun ArticleViewModel.handleIncomingArticleListData(viewState: ArticleViewState) {
    Log.d(TAG, "ArticleViewModel, DataState: ${viewState}")
    Log.d(
        TAG, "ArticleViewModel, DataState: isQueryInProgress?: " +
                "${viewState.articleFields.isQueryInProgress}"
    )
    Log.d(
        TAG, "ArticleViewModel, DataState: isQueryExhausted?: " +
                "${viewState.articleFields.isQueryExhausted}"
    )
    setQueryInProgress(viewState.articleFields.isQueryInProgress)
    setQueryExhausted(viewState.articleFields.isQueryExhausted)
    setArticleListData(viewState.articleFields.articleList)
}
