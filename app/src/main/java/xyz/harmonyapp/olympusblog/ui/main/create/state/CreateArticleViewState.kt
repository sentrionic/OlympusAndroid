package xyz.harmonyapp.olympusblog.ui.main.create.state

import android.net.Uri

data class CreateArticleViewState(

    // CreateArticleFragment vars
    var articleFields: NewArticleFields = NewArticleFields()

) {
    data class NewArticleFields(
        var newArticleTitle: String? = null,
        var newArticleBody: String? = null,
        var newArticleDescription: String? = null,
        var newArticleTags: String? = null,
        var newImageUri: Uri? = null
    )
}