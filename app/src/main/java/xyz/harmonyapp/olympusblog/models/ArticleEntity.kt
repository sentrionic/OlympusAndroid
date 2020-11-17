package xyz.harmonyapp.olympusblog.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "articles")
data class ArticleEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: Int,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "description")
    var description: String,

    @ColumnInfo(name = "slug")
    var slug: String,

    @ColumnInfo(name = "body")
    var body: String,

    @ColumnInfo(name = "image")
    var image: String,

    @ColumnInfo(name = "createdAt")
    var createdAt: String,

    @ColumnInfo(name = "favoritesCount")
    var favoritesCount: Int,

    @ColumnInfo(name = "favorited")
    var favorited: Boolean,

    @ColumnInfo(name = "bookmarked")
    var bookmarked: Boolean,

    @ColumnInfo(name = "tagList")
    var tagList: String,

    @ColumnInfo(name = "authorId")
    var authorId: Int,

    ) : Parcelable {
    override fun toString(): String {
        return "ArticleEntity(id=$id, title='$title', description='$description', slug='$slug', body='$body', image='$image', createdAt='$createdAt', favoritesCount=$favoritesCount, favorited=$favorited, bookmarked=$bookmarked, tagList='$tagList', authorId=$authorId)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArticleEntity

        if (id != other.id) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (slug != other.slug) return false
        if (body != other.body) return false
        if (image != other.image) return false
        if (createdAt != other.createdAt) return false
        if (favoritesCount != other.favoritesCount) return false
        if (favorited != other.favorited) return false
        if (bookmarked != other.bookmarked) return false
        if (tagList != other.tagList) return false
        if (authorId != other.authorId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + slug.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + favoritesCount
        result = 31 * result + favorited.hashCode()
        result = 31 * result + bookmarked.hashCode()
        result = 31 * result + tagList.hashCode()
        result = 31 * result + authorId
        return result
    }


}