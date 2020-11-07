package xyz.harmonyapp.olympusblog.api.main.dto

import com.squareup.moshi.Json

data class CommentDTO(
    @Json(name = "body")
    var body: String,
)