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
    var tagList: List<String>,
    var author: Author,
) : Parcelable {
    override fun toString(): String {
        return "Article(id=$id, title='$title', description='$description', slug='$slug', body='$body', image='$image', createdAt='$createdAt', favoritesCount=$favoritesCount, favorited=$favorited, bookmarked=$bookmarked, tagList=$tagList, author=$author)"
    }
}