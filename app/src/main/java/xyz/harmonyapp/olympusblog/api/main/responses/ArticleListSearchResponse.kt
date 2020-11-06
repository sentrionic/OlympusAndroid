package xyz.harmonyapp.olympusblog.api.main.responses

import com.squareup.moshi.Json
import xyz.harmonyapp.olympusblog.models.Article

class ArticleListSearchResponse(

    @Json(name = "articles")
    var articles: List<ArticleResponse>,

    @Json(name = "hasMore")
    var hasMore: Boolean
) {

    fun toList(): List<Article> {
        val articleList: ArrayList<Article> = ArrayList()
        for (articleResponse in articles) {
            articleList.add(
                articleResponse.toArticle()
            )
        }
        return articleList
    }

    override fun toString(): String {
        return "ArticleListSearchResponse(articles=$articles, hasMore=$hasMore)"
    }
}