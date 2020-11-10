package xyz.harmonyapp.olympusblog.api.main

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import xyz.harmonyapp.olympusblog.api.main.dto.ChangePasswordDTO
import xyz.harmonyapp.olympusblog.api.main.dto.CommentDTO
import xyz.harmonyapp.olympusblog.api.main.responses.ArticleListSearchResponse
import xyz.harmonyapp.olympusblog.api.main.responses.ArticleResponse
import xyz.harmonyapp.olympusblog.api.main.responses.CommentResponse
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.AccountProperties
import xyz.harmonyapp.olympusblog.models.Author
import xyz.harmonyapp.olympusblog.utils.Constants.Companion.PAGINATION_PAGE_SIZE

@MainScope
interface MainService {

    //Users
    @GET("user")
    suspend fun getAccountProperties(): AccountProperties

    @PUT("user")
    @Multipart
    suspend fun saveAccountProperties(
        @Part("email") email: RequestBody,
        @Part("username") username: RequestBody,
        @Part("bio") bio: RequestBody,
        @Part image: MultipartBody.Part?
    ): AccountProperties

    @PUT("users/change-password/")
    suspend fun updatePassword(
        @Body body: ChangePasswordDTO
    ): AccountProperties

    @POST("users/logout")
    suspend fun logout(): Boolean

    // Profiles
    @GET("profiles")
    suspend fun searchProfiles(
        @Query("search") query: String,
    ): List<Author>

    @GET("profiles/{username}")
    suspend fun getProfileByUsername(
        @Path("username") username: String
    ): Author

    @POST("profiles/{username}/follow")
    suspend fun followUser(
        @Path("username") username: String
    ): Author

    @DELETE("profiles/{username}/follow")
    suspend fun unfollowUser(
        @Path("username") username: String
    ): Author

    // Articles
    @GET("articles")
    suspend fun searchListArticlePosts(
        @Query("search") query: String,
        @Query("order") order: String,
        @Query("limit") limit: Int = PAGINATION_PAGE_SIZE,
        @Query("p") page: Int
    ): ArticleListSearchResponse

    @GET("articles/feed")
    suspend fun getFeed(
        @Query("limit") limit: Int = PAGINATION_PAGE_SIZE,
        @Query("cursor") cursor: String
    ): ArticleListSearchResponse

    @GET("articles/bookmarked")
    suspend fun getBookmarked(
        @Query("limit") limit: Int = PAGINATION_PAGE_SIZE,
        @Query("cursor") cursor: String
    ): ArticleListSearchResponse

    @GET("articles/{slug}")
    suspend fun getArticle(
        @Path("slug") slug: String
    ): ArticleResponse

    @DELETE("articles/{slug}")
    suspend fun deleteArticle(
        @Path("slug") slug: String
    ): ArticleResponse

    @Multipart
    @PUT("articles/{slug}")
    suspend fun updateArticle(
        @Path("slug") slug: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("body") body: RequestBody,
        @Part image: MultipartBody.Part?
    ): ArticleResponse

    @Multipart
    @POST("articles")
    suspend fun createArticle(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("tagList[]") tagList: List<String>,
        @Part("body") body: RequestBody,
        @Part image: MultipartBody.Part?
    ): ArticleResponse

    @POST("articles/{slug}/favorite")
    suspend fun favoriteArticle(
        @Path("slug") slug: String
    ): ArticleResponse

    @DELETE("articles/{slug}/favorite")
    suspend fun unfavoriteArticle(
        @Path("slug") slug: String
    ): ArticleResponse

    @POST("articles/{slug}/bookmark")
    suspend fun bookmarkArticle(
        @Path("slug") slug: String
    ): ArticleResponse

    @DELETE("articles/{slug}/bookmark")
    suspend fun unbookmarkArticle(
        @Path("slug") slug: String
    ): ArticleResponse

    //Comments
    @GET("articles/{slug}/comments")
    suspend fun getArticleComments(
        @Path("slug") slug: String,
    ): List<CommentResponse>

    @POST("articles/{slug}/comments")
    suspend fun createComment(
        @Path("slug") slug: String,
        @Body body: CommentDTO
    ): CommentResponse

    @DELETE("articles/{slug}/comments/{id}")
    suspend fun deleteComment(
        @Path("slug") slug: String,
        @Path("id") id: Int,
    ): CommentResponse
}