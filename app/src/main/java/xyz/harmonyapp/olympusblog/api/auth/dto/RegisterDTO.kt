package xyz.harmonyapp.olympusblog.api.auth.dto

import com.squareup.moshi.Json

data class RegisterDTO (

    @Json(name = "email")
    var email: String,

    @Json(name = "username")
    var username: String,

    @Json(name = "password")
    var password: String,
)