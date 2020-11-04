package xyz.harmonyapp.olympusblog.api.main.responses

import com.squareup.moshi.Json

class ArticleListSearchResponse(

    @Json(name = "articles")
    var articles: List<ArticleResponse>,

    @Json(name = "hasMore")
    var hasMore: Boolean
) {

    override fun toString(): String {
        return "ArticleListSearchResponse(articles=$articles, hasMore=$hasMore)"
    }
}