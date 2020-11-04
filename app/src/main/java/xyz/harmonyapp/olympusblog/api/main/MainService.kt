package xyz.harmonyapp.olympusblog.api.main

import androidx.lifecycle.LiveData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import xyz.harmonyapp.olympusblog.api.main.dto.ChangePasswordDTO
import xyz.harmonyapp.olympusblog.api.main.responses.ArticleListSearchResponse
import xyz.harmonyapp.olympusblog.api.main.responses.ArticleResponse
import xyz.harmonyapp.olympusblog.models.AccountProperties
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.utils.Constants.Companion.PAGINATION_PAGE_SIZE
import xyz.harmonyapp.olympusblog.utils.GenericApiResponse

interface MainService {
    @GET("user")
    fun getAccountProperties(): LiveData<GenericApiResponse<AccountProperties>>

    @PUT("user")
    @Multipart
    fun saveAccountProperties(
        @Part("email") email: RequestBody,
        @Part("username") username: RequestBody,
        @Part("bio") bio: RequestBody,
        @Part image: MultipartBody.Part?
    ): LiveData<GenericApiResponse<AccountProperties>>

    @PUT("users/change-password/")
    fun updatePassword(
        @Body body: ChangePasswordDTO
    ): LiveData<GenericApiResponse<AccountProperties>>

    @GET("articles")
    fun searchListArticlePosts(
        @Query("search") query: String,
        @Query("order") order: String,
        @Query("limit") limit: Int = PAGINATION_PAGE_SIZE,
        @Query("p") page: Int
    ): LiveData<GenericApiResponse<ArticleListSearchResponse>>

    @GET("articles/{slug}")
    fun getArticle(
        @Path("slug") slug: String
    ): LiveData<GenericApiResponse<ArticleResponse>>

    @DELETE("articles/{slug}")
    fun deleteArticle(
        @Path("slug") slug: String
    ): LiveData<GenericApiResponse<ArticleResponse>>

    @Multipart
    @PUT("articles/{slug}")
    fun updateArticle(
        @Path("slug") slug: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("body") body: RequestBody,
        @Part image: MultipartBody.Part?
    ): LiveData<GenericApiResponse<ArticleResponse>>

    @Multipart
    @POST("articles")
    fun createArticle(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("tagList[]") tagList: List<String>,
        @Part("body") body: RequestBody,
        @Part image: MultipartBody.Part?
    ): LiveData<GenericApiResponse<ArticleResponse>>
}