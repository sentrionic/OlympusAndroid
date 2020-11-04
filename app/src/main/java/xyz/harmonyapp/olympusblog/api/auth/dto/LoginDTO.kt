package xyz.harmonyapp.olympusblog.api.auth.dto

import com.squareup.moshi.Json

data class LoginDTO (

    @Json(name = "email")
    var email: String,

    @Json(name = "password")
    var password: String,
)