package xyz.harmonyapp.olympusblog.models

import android.os.Parcelable
import androidx.room.*
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

    @ColumnInfo(name = "authorId")
    var authorId: Int,

) : Parcelable {
    override fun toString(): String {
        return "Article(id=$id, title='$title', description='$description', slug='$slug', body='$body', image='$image', createdAt=$createdAt, favoritesCount=$favoritesCount, favorited=$favorited)"
    }
}