package xyz.harmonyapp.olympusblog.ui.main.article.state

import android.net.Uri
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.persistence.ArticleQueryUtils

data class ArticleViewState(

    var articleFields: ArticleFields = ArticleFields(),
    var viewArticleFields: ViewArticleFields = ViewArticleFields(),
    var updatedArticleFields: UpdatedArticleFields = UpdatedArticleFields(),
) {
    data class ArticleFields(
        var articleList: List<Article> = ArrayList(),
        var searchQuery: String = "",
        var page: Int = 1,
        var isQueryInProgress: Boolean = false,
        var isQueryExhausted: Boolean = false,
        var order: String = ArticleQueryUtils.ARTICLES_DESC
    )

    data class ViewArticleFields(
        var article: Article? = null,
        var isAuthorOfArticle: Boolean = false
    )

    data class UpdatedArticleFields(
        var updatedArticleTitle: String? = null,
        var updatedArticleDescription: String? = null,
        var updatedArticleBody: String? = null,
        var updatedImageUri: Uri? = null
    )

}