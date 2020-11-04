package xyz.harmonyapp.olympusblog.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.utils.Constants.Companion.PAGINATION_PAGE_SIZE

@Dao
interface ArticlesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: Article): Long

    @Query(
        """
        SELECT * FROM articles 
        WHERE title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        LIMIT (:page * :limit)
        """
    )
    fun getAllArticles(
        query: String,
        page: Int,
        limit: Int = PAGINATION_PAGE_SIZE,
    ): LiveData<List<Article>>

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
    fun searchArticlesOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): LiveData<List<Article>>

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
    fun searchArticlesOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): LiveData<List<Article>>

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
    fun searchArticlesOrderByFavoritesCountDESC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): LiveData<List<Article>>

    @Query("SELECT * FROM articles WHERE slug = :slug")
    fun getArticleBySlug(slug: String): LiveData<Article>

    @Delete
    suspend fun deleteArticle(article: Article)

    @Query("""
        UPDATE articles SET title = :title, body = :body, image = :image, description = :description 
        WHERE id = :id
        """)

    fun updateArticle(id: Int, title: String, body: String, description: String, image: String)
}