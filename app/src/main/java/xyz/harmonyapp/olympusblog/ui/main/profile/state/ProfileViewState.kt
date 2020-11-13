package xyz.harmonyapp.olympusblog.ui.main.profile.state

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.models.Author

const val PROFILE_VIEW_STATE_BUNDLE_KEY =
    "xyz.harmonyapp.olympusblog.ui.main.profile.state.ProfileViewState"

@Parcelize
data class ProfileViewState(

    var profileFields: ProfileFields = ProfileFields(),
    var viewProfileFields: ViewProfileFields = ViewProfileFields(),

) : Parcelable {

    @Parcelize
    data class ProfileFields(
        var profileList: List<Author>? = null,
        var searchQuery: String? = null,
    ) : Parcelable

    @Parcelize
    data class ViewProfileFields(
        var profile: Author? = null,
        var articleList: List<Article>? = null
    ) : Parcelable
}