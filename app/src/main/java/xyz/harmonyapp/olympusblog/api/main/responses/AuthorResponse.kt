package xyz.harmonyapp.olympusblog.api.main.responses

import com.squareup.moshi.Json

data class AuthorResponse(

    @Json(name = "id")
    var id: Int,

    @Json(name = "username")
    var username: String,

    @Json(name = "image")
    var image: String,

    @Json(name = "bio")
    var bio: String,

    @Json(name = "following")
    var following: Boolean,

    @Json(name = "followers")
    var followers: Int,

    @Json(name = "followee")
    var followee: Int,
)