package xyz.harmonyapp.olympusblog.api.main.dto

import com.squareup.moshi.Json

data class ChangePasswordDTO(

    @Json(name = "currentPassword")
    var currentPassword: String,

    @Json(name = "newPassword")
    var newPassword: String,

    @Json(name = "confirmNewPassword")
    var confirmNewPassword: String,
)