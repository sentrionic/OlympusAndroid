package xyz.harmonyapp.olympusblog.persistence

import xyz.harmonyapp.olympusblog.models.Article

class ArticleQueryUtils {


    companion object {
        private val TAG: String = "AppDebug"

        // values
        const val ARTICLES_DESC: String = "DESC"
        const val ARTICLES_ASC: String = "ASC"
        const val ARTICLES_TOP = "TOP"
    }
}

suspend fun ArticlesDao.returnOrderedQuery(
    query: String,
    order: String,
    page: Int
): List<Article> {

    when {

        (order == ArticleQueryUtils.ARTICLES_ASC) -> {
            return searchArticlesOrderByDateASC(
                query = query,
                page = page
            )
        }

        (order == ArticleQueryUtils.ARTICLES_DESC) -> {
            return searchArticlesOrderByDateDESC(
                query = query,
                page = page
            )
        }

        (order == ArticleQueryUtils.ARTICLES_TOP) -> {
            return searchArticlesOrderByFavoritesCountDESC(
                query = query,
                page = page
            )
        }

        else ->
            return searchArticlesOrderByDateDESC(
                query = query,
                page = page
            )
    }
}