package xyz.harmonyapp.olympusblog.ui.main.article.state

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.persistence.ArticleQueryUtils

const val ARTICLE_VIEW_STATE_BUNDLE_KEY = "xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState"

@Parcelize
data class ArticleViewState(

    var articleFields: ArticleFields = ArticleFields(),
    var viewArticleFields: ViewArticleFields = ViewArticleFields(),
    var updatedArticleFields: UpdatedArticleFields = UpdatedArticleFields(),
) : Parcelable {

    @Parcelize
    data class ArticleFields(
        var articleList: List<Article> = ArrayList(),
        var searchQuery: String = "",
        var page: Int = 1,
        var isQueryInProgress: Boolean = false,
        var isQueryExhausted: Boolean = false,
        var order: String = ArticleQueryUtils.ARTICLES_DESC,
        var layoutManagerState: Parcelable? = null
    ) : Parcelable

    @Parcelize
    data class ViewArticleFields(
        var article: Article? = null,
        var isAuthorOfArticle: Boolean = false
    ): Parcelable

    @Parcelize
    data class UpdatedArticleFields(
        var updatedArticleTitle: String? = null,
        var updatedArticleDescription: String? = null,
        var updatedArticleBody: String? = null,
        var updatedImageUri: Uri? = null
    ): Parcelable

}