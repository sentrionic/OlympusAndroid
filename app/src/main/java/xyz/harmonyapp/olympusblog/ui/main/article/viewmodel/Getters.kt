package xyz.harmonyapp.olympusblog.ui.main.article.viewmodel

import android.net.Uri
import xyz.harmonyapp.olympusblog.models.Article

fun ArticleViewModel.getSearchQuery(): String {
    getCurrentViewStateOrNew().let {
        return it.articleFields.searchQuery
    }
}

fun ArticleViewModel.getPage(): Int {
    getCurrentViewStateOrNew().let {
        return it.articleFields.page
    }
}

fun ArticleViewModel.getIsQueryExhausted(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.articleFields.isQueryExhausted
    }
}

fun ArticleViewModel.getIsQueryInProgress(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.articleFields.isQueryInProgress
    }
}

fun ArticleViewModel.getOrder(): String {
    getCurrentViewStateOrNew().let {
        return it.articleFields.order
    }
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
    getCurrentViewStateOrNew().let {
        return it.viewArticleFields.isAuthorOfArticle
    }
}

fun ArticleViewModel.getArticle(): Article {
    getCurrentViewStateOrNew().let {
        return it.viewArticleFields.article?.let {
            return it
        } ?: getDummyArticle()
    }
}

fun getDummyArticle(): Article {
    return Article(-1, "", "", "", "", "", "", 0, false, "", "")
}

fun ArticleViewModel.getUpdatedArticleUri(): Uri? {
    getCurrentViewStateOrNew().let {
        it.updatedArticleFields.updatedImageUri?.let {
            return it
        }
    }
    return null
}