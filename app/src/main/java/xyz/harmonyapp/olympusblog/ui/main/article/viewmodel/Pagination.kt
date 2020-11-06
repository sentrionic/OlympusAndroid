package xyz.harmonyapp.olympusblog.ui.main.article.viewmodel

import android.util.Log
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent.ArticleSearchEvent
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState

fun ArticleViewModel.resetPage() {
    val update = getCurrentViewStateOrNew()
    update.articleFields.page = 1
    setViewState(update)
}

fun ArticleViewModel.refreshFromCache() {
    if (!isJobAlreadyActive(ArticleSearchEvent())) {
        setQueryExhausted(false)
        setStateEvent(ArticleSearchEvent(false))
    }
}

fun ArticleViewModel.loadFirstPage() {
    if (!isJobAlreadyActive(ArticleSearchEvent())) {
        setQueryExhausted(false)
        resetPage()
        setStateEvent(ArticleSearchEvent())
        Log.e(
            TAG,
            "ArticleViewModel: loadFirstPage: ${viewState.value!!.articleFields.searchQuery}"
        )
    }
}

private fun ArticleViewModel.incrementPageNumber() {
    val update = getCurrentViewStateOrNew()
    val page = update.copy().articleFields.page ?: 1
    update.articleFields.page = page.plus(1)
    setViewState(update)
}

fun ArticleViewModel.nextPage() {
    if (!isJobAlreadyActive(ArticleSearchEvent())
        && !getIsQueryExhausted()
    ) {
        Log.d(TAG, "ArticleViewModel: Attempting to load next page...")
        incrementPageNumber()
        setStateEvent(ArticleSearchEvent())
    }
}

fun ArticleViewModel.handleIncomingArticleListData(viewState: ArticleViewState) {
    viewState.articleFields.let { articleFields ->
        articleFields.articleList?.let { setArticleListData(it) }
    }
}