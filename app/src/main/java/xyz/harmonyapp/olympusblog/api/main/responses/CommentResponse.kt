package xyz.harmonyapp.olympusblog.api.main.responses

import com.squareup.moshi.Json

data class CommentResponse(

    @Json(name = "id")
    var id: Int,

    @Json(name = "title")
    var title: String,

    @Json(name = "createdAt")
    var createdAt: String,

    @Json(name = "body")
    var body: String,

    @Json(name = "author")
    var author: AuthorResponse,

)