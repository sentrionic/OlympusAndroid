package xyz.harmonyapp.olympusblog.models

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ArticleAuthor(
    @Embedded
    val article: ArticleEntity,
    @Relation(
        parentColumn = "authorId",
        entityColumn = "id"
    )
    val author: Author
) : Parcelable {
    fun toArticle(): Article {
        return Article(
            id = article.id,
            title = article.title,
            description = article.description,
            slug = article.slug,
            body = article.body,
            image = article.image,
            favoritesCount = article.favoritesCount,
            createdAt = article.createdAt,
            favorited = article.favorited,
            bookmarked = article.bookmarked,
            tagList = article.tagList.replace("[", "").replace("]", "").split(","),
            author = author
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArticleAuthor

        if (article != other.article) return false
        if (author != other.author) return false

        return true
    }

    override fun hashCode(): Int {
        var result = article.hashCode()
        result = 31 * result + author.hashCode()
        return result
    }


}