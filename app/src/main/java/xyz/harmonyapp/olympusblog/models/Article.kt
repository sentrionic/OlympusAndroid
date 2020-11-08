package xyz.harmonyapp.olympusblog.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Article(
    var id: Int,
    var title: String,
    var description: String,
    var slug: String,
    var body: String,
    var image: String,
    var createdAt: String,
    var favoritesCount: Int,
    var favorited: Boolean,
    var bookmarked: Boolean,
    var author: Author,
) : Parcelable