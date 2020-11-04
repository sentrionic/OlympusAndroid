package xyz.harmonyapp.olympusblog.ui.main.create.state

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

const val CREATE_ARTICLE_VIEW_STATE_BUNDLE_KEY = "xyz.harmonyapp.olympusblog.ui.main.article.state.CreateArticleViewState"

@Parcelize
data class CreateArticleViewState(

    // CreateArticleFragment vars
    var articleFields: NewArticleFields = NewArticleFields()

) : Parcelable {

    @Parcelize
    data class NewArticleFields(
        var newArticleTitle: String? = null,
        var newArticleBody: String? = null,
        var newArticleDescription: String? = null,
        var newArticleTags: String? = null,
        var newImageUri: Uri? = null
    ) : Parcelable
}