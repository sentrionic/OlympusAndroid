package xyz.harmonyapp.olympusblog.api.main

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import xyz.harmonyapp.olympusblog.api.main.dto.ChangePasswordDTO
import xyz.harmonyapp.olympusblog.api.main.responses.ArticleListSearchResponse
import xyz.harmonyapp.olympusblog.api.main.responses.ArticleResponse
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.AccountProperties
import xyz.harmonyapp.olympusblog.utils.Constants.Companion.PAGINATION_PAGE_SIZE

@MainScope
interface MainService {
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

    @GET("articles")
    suspend fun searchListArticlePosts(
        @Query("search") query: String,
        @Query("order") order: String,
        @Query("limit") limit: Int = PAGINATION_PAGE_SIZE,
        @Query("p") page: Int
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
}