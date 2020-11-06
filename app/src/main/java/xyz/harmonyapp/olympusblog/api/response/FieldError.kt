package xyz.harmonyapp.olympusblog.api.response

import com.squareup.moshi.Json

data class FieldError(

    @Json(name = "field")
    var field: String,

    @Json(name = "message")
    var message: String,
)