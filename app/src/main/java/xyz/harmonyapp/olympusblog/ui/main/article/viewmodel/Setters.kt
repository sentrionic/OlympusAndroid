package xyz.harmonyapp.olympusblog.ui.main.article.viewmodel

import android.net.Uri
import android.os.Parcelable
import xyz.harmonyapp.olympusblog.api.main.responses.CommentResponse
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

fun ArticleViewModel.setCommentsList(comments: List<CommentResponse>) {
    val update = getCurrentViewStateOrNew()
    update.viewArticleFields.commentList = comments
    setViewState(update)
}

fun ArticleViewModel.setQueryExhausted(isExhausted: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.articleFields.isQueryExhausted = isExhausted
    setViewState(update)
}

fun ArticleViewModel.setArticleOrder(order: String) {
    val update = getCurrentViewStateOrNew()
    update.articleFields.order = order
    setViewState(update)
}

fun ArticleViewModel.removeDeletedArticle() {
    val update = getCurrentViewStateOrNew()
    val list = update.articleFields.articleList?.toMutableList()
    if (list != null) {
        for (i in 0 until list.size) {
            if (list[i] == getArticle()) {
                list.remove(getArticle())
                break
            }
        }
        setArticleListData(list)
    }
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

fun ArticleViewModel.updateListItem() {
    val update = getCurrentViewStateOrNew()
    val list = update.articleFields.articleList?.toMutableList()
    if (list != null) {
        val newArticle = getArticle()
        for (i in 0 until list.size) {
            if (list[i].id == newArticle.id) {
                list[i] = newArticle
                break
            }
        }
        update.articleFields.articleList = list
        setViewState(update)
    }
}

fun ArticleViewModel.setLayoutManagerState(layoutManagerState: Parcelable) {
    val update = getCurrentViewStateOrNew()
    update.articleFields.layoutManagerState = layoutManagerState
    setViewState(update)
}

fun ArticleViewModel.clearLayoutManagerState() {
    val update = getCurrentViewStateOrNew()
    update.articleFields.layoutManagerState = null
    setViewState(update)
}

fun ArticleViewModel.setUpdatedUri(uri: Uri) {
    val update = getCurrentViewStateOrNew()
    val updatedArticleFields = update.updatedArticleFields
    updatedArticleFields.updatedImageUri = uri
    update.updatedArticleFields = updatedArticleFields
    setViewState(update)
}

fun ArticleViewModel.setUpdatedTitle(title: String) {
    val update = getCurrentViewStateOrNew()
    val updatedArticleFields = update.updatedArticleFields
    updatedArticleFields.updatedArticleTitle = title
    update.updatedArticleFields = updatedArticleFields
    setViewState(update)
}

fun ArticleViewModel.setUpdatedDescription(description: String) {
    val update = getCurrentViewStateOrNew()
    val updatedArticleFields = update.updatedArticleFields
    updatedArticleFields.updatedArticleDescription = description
    update.updatedArticleFields = updatedArticleFields
    setViewState(update)
}

fun ArticleViewModel.setUpdatedBody(body: String) {
    val update = getCurrentViewStateOrNew()
    val updatedArticleFields = update.updatedArticleFields
    updatedArticleFields.updatedArticleBody = body
    update.updatedArticleFields = updatedArticleFields
    setViewState(update)
}

fun ArticleViewModel.setComment(comment: CommentResponse) {
    val update = getCurrentViewStateOrNew()
    update.viewCommentsFields.comment = comment
    setViewState(update)
}

fun ArticleViewModel.addComment(comment: CommentResponse) {
    val update = getCurrentViewStateOrNew()
    val list = update.viewArticleFields.commentList?.toMutableList()
    if (list != null) {
        list.add(comment)
        setCommentsList(list)
    }
}


fun ArticleViewModel.removeDeletedComment() {
    val update = getCurrentViewStateOrNew()
    val list = update.viewArticleFields.commentList?.toMutableList()
    if (list != null) {
        for (i in 0 until list.size) {
            if (list[i] == getComment()) {
                list.remove(getComment())
                break
            }
        }
        setCommentsList(list)
    }
}

