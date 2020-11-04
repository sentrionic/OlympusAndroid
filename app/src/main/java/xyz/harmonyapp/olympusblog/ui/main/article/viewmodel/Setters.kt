package xyz.harmonyapp.olympusblog.ui.main.article.viewmodel

import android.net.Uri
import xyz.harmonyapp.olympusblog.models.Article

fun ArticleViewModel.setQuery(query: String) {
    val update = getCurrentViewStateOrNew()
    update.articleFields.searchQuery = query
    setViewState(update)
}

fun ArticleViewModel.setArticleListData(articleList: List<Article>) {
    val update = getCurrentViewStateOrNew()
    update.articleFields.articleList = articleList
    setViewState(update)
}

fun ArticleViewModel.setArticle(article: Article) {
    val update = getCurrentViewStateOrNew()
    update.viewArticleFields.article = article
    setViewState(update)
}

fun ArticleViewModel.setIsAuthorOfArticle(isAuthorOfArticle: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.viewArticleFields.isAuthorOfArticle = isAuthorOfArticle
    setViewState(update)
}

fun ArticleViewModel.setQueryExhausted(isExhausted: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.articleFields.isQueryExhausted = isExhausted
    setViewState(update)
}

fun ArticleViewModel.setQueryInProgress(isInProgress: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.articleFields.isQueryInProgress = isInProgress
    setViewState(update)
}

fun ArticleViewModel.setArticleOrder(order: String) {
    val update = getCurrentViewStateOrNew()
    update.articleFields.order = order
    setViewState(update)
}

fun ArticleViewModel.removeDeletedArticle() {
    val update = getCurrentViewStateOrNew()
    val list = update.articleFields.articleList.toMutableList()
    for (i in 0 until list.size) {
        if (list[i] == getArticle()) {
            list.remove(getArticle())
            break
        }
    }
    setArticleListData(list)
}

fun ArticleViewModel.setUpdatedArticleFields(
    title: String?,
    description: String?,
    body: String?,
    uri: Uri?
) {
    val update = getCurrentViewStateOrNew()
    val updatedArticleFields = update.updatedArticleFields
    title?.let { updatedArticleFields.updatedArticleTitle = it }
    description?.let { updatedArticleFields.updatedArticleDescription = it }
    body?.let { updatedArticleFields.updatedArticleBody = it }
    uri?.let { updatedArticleFields.updatedImageUri = it }
    update.updatedArticleFields = updatedArticleFields
    setViewState(update)
}

fun ArticleViewModel.updateListItem(newArticle: Article) {
    val update = getCurrentViewStateOrNew()
    val list = update.articleFields.articleList.toMutableList()
    for (i in 0 until list.size) {
        if (list[i].id == newArticle.id) {
            list[i] = newArticle
            break
        }
    }
    update.articleFields.articleList = list
    setViewState(update)
}


fun ArticleViewModel.onArticleUpdateSuccess(article: Article) {
    setUpdatedArticleFields(
        uri = null,
        title = article.title,
        description = article.description,
        body = article.body
    ) // update UpdateBlogFragment (not really necessary since navigating back)
    setArticle(article) // update ViewBlogFragment
    updateListItem(article) // update BlogFragment
}

