package xyz.harmonyapp.olympusblog.ui.main.article.state

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import xyz.harmonyapp.olympusblog.api.main.responses.CommentResponse
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.models.Author

const val ARTICLE_VIEW_STATE_BUNDLE_KEY =
    "xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState"

@Parcelize
data class ArticleViewState(

    var articleFields: ArticleFields = ArticleFields(),
    var viewArticleFields: ViewArticleFields = ViewArticleFields(),
    var updatedArticleFields: UpdatedArticleFields = UpdatedArticleFields(),
    var newArticleFields: NewArticleFields = NewArticleFields(),
    var viewCommentsFields: ViewCommentsFields = ViewCommentsFields(),
    var searchFields: SearchFields = SearchFields(),
    var viewProfileFields: ViewProfileFields = ViewProfileFields(),

    ) : Parcelable {

    @Parcelize
    data class ArticleFields(
        var articleList: List<Article>? = null,
        var searchQuery: String? = null,
        var page: Int? = null,
        var isQueryExhausted: Boolean? = null,
        var order: String? = null,
        var layoutManagerState: Parcelable? = null
    ) : Parcelable

    @Parcelize
    data class ViewArticleFields(
        var article: Article? = null,
        var isAuthorOfArticle: Boolean? = null,
        var commentList: List<CommentResponse>? = null
    ) : Parcelable

    @Parcelize
    data class ViewCommentsFields(
        var comment: CommentResponse? = null,
        var userId: Int? = null,
    ) : Parcelable

    @Parcelize
    data class NewArticleFields(
        var newArticle: Article? = null,
        var newArticleTitle: String? = null,
        var newArticleBody: String? = null,
        var newArticleDescription: String? = null,
        var newArticleTags: String? = null,
        var newImageUri: Uri? = null
    ) : Parcelable

    @Parcelize
    data class UpdatedArticleFields(
        var updatedArticleTitle: String? = null,
        var updatedArticleDescription: String? = null,
        var updatedArticleBody: String? = null,
        var updatedImageUri: Uri? = null,
        var updatedArticleTags: String? = null
    ) : Parcelable

    @Parcelize
    data class SearchFields(
        var profileList: List<Author>? = null,
        var isQueryExhausted: Boolean? = null
    ) : Parcelable

    @Parcelize
    data class ViewProfileFields(
        var profile: Author? = null,
        var articleList: List<Article>? = null
    ) : Parcelable

}