package xyz.harmonyapp.olympusblog.ui.main.article.viewmodel

import android.net.Uri
import xyz.harmonyapp.olympusblog.api.main.responses.CommentResponse
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.models.Author
import xyz.harmonyapp.olympusblog.persistence.ArticleQueryUtils.Companion.ARTICLES_DESC

fun ArticleViewModel.getSearchQuery(): String {
    return getCurrentViewStateOrNew().articleFields.searchQuery ?: ""
}

fun ArticleViewModel.getPage(): Int {
    return getCurrentViewStateOrNew().articleFields.page ?: -1
}

fun ArticleViewModel.getIsQueryExhausted(): Boolean {
    return getCurrentViewStateOrNew().articleFields.isQueryExhausted ?: false
}

fun ArticleViewModel.getOrder(): String {
    return getCurrentViewStateOrNew().articleFields.order ?: ARTICLES_DESC
}

fun ArticleViewModel.getSlug(): String {
    getCurrentViewStateOrNew().let {
        it.viewArticleFields.article?.let {
            return it.slug
        }
    }
    return ""
}

fun ArticleViewModel.isAuthor(): Boolean {
    return getCurrentViewStateOrNew().viewArticleFields.isAuthorOfArticle ?: false
}

fun ArticleViewModel.getArticle(): Article {
    getCurrentViewStateOrNew().let {
        return it.viewArticleFields.article?.let {
            return it
        } ?: getDummyArticle()
    }
}

fun getDummyArticle(): Article {
    return Article(
        -1,
        "",
        "",
        "",
        "",
        "",
        "",
        0,
        false,
        false,
        emptyList(),
        getDummyAuthor()
    )
}

fun ArticleViewModel.getUpdatedArticleUri(): Uri? {
    getCurrentViewStateOrNew().let {
        it.updatedArticleFields.updatedImageUri?.let {
            return it
        }
    }
    return null
}

fun ArticleViewModel.getComment(): CommentResponse {
    getCurrentViewStateOrNew().let {
        return it.viewCommentsFields.comment?.let {
            return it
        } ?: getDummyComment()
    }
}

fun getDummyAuthor(): Author {
    return Author(-1, "", "", "", false, 0, 0)
}

fun getDummyComment(): CommentResponse {
    return CommentResponse(-1, "", "", getDummyAuthor())
}