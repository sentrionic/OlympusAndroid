package xyz.harmonyapp.olympusblog.persistence

import androidx.room.*
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.models.ArticleAuthor
import xyz.harmonyapp.olympusblog.models.Author
import xyz.harmonyapp.olympusblog.utils.Constants.Companion.PAGINATION_PAGE_SIZE

@Dao
interface ArticlesDao {

    @Query("DELETE FROM articles")
    suspend fun nukeTable()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: Article): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthor(author: Author): Long

    @Query("SELECT * FROM articles")
    suspend fun getArticles(): List<ArticleAuthor>

    @Query(
        """
        SELECT * FROM articles 
        WHERE title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        ORDER BY createdAt DESC 
        LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchArticlesOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<Article>

    @Query(
        """
        SELECT * FROM articles 
        WHERE title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        ORDER BY createdAt ASC 
        LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchArticlesOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<Article>

    @Query(
        """
        SELECT * FROM articles 
        WHERE title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        ORDER BY favoritesCount DESC 
        LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchArticlesOrderByFavoritesCountDESC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<Article>

    @Query("SELECT * FROM articles WHERE slug = :slug")
    suspend fun getArticleBySlug(slug: String): Article

    @Delete
    suspend fun deleteArticle(article: Article)

    @Query(
        """
        UPDATE articles SET title = :title, body = :body, image = :image, description = :description 
        WHERE id = :id
        """
    )
    suspend fun updateArticle(
        id: Int,
        title: String,
        body: String,
        description: String,
        image: String
    )

    @Query(
        """
        UPDATE articles SET favoritesCount = :favoritesCount, favorited = :favorited 
        WHERE id = :id
        """
    )
    suspend fun updateFavorite(
        id: Int,
        favoritesCount: Int,
        favorited: Boolean,
    )

    @Query(
        """
        UPDATE articles SET bookmarked = :bookmarked 
        WHERE id = :id
        """
    )
    suspend fun updateBookmark(
        id: Int,
        bookmarked: Boolean,
    )
}