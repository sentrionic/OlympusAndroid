package xyz.harmonyapp.olympusblog.api.main.responses

import com.squareup.moshi.Json
import xyz.harmonyapp.olympusblog.models.Article

data class ArticleResponse(

    @Json(name = "id")
    var id: Int,

    @Json(name = "title")
    var title: String,

    @Json(name = "description")
    var description: String,

    @Json(name = "slug")
    var slug: String,

    @Json(name = "body")
    var body: String,

    @Json(name = "image")
    var image: String,

    @Json(name = "createdAt")
    var createdAt: String,

    @Json(name = "favoritesCount")
    var favoritesCount: Int,

    @Json(name = "favorited")
    var favorited: Boolean,

    @Json(name = "bookmarked")
    var bookmarked: Boolean,

    @Json(name = "author")
    var author: AuthorResponse,

    ) {
    override fun toString(): String {
        return "Article(id=$id, title='$title', description='$description', slug='$slug', body='$body', image='$image', createdAt=$createdAt, favoritesCount=$favoritesCount, favorited=$favorited, author=$author)"
    }

    fun toArticle(): Article {
        return Article(
            id = id,
            title = title,
            description = description,
            body = body,
            image = image,
            createdAt = createdAt,
            slug = slug,
            favorited = favorited,
            bookmarked = bookmarked,
            favoritesCount = favoritesCount,
            username = author.username,
            profileImage = author.image
        )
    }
}