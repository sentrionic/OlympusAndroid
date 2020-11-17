package xyz.harmonyapp.olympusblog.api.main.responses

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import xyz.harmonyapp.olympusblog.models.Author

@Parcelize
data class CommentResponse(

    @Json(name = "id")
    var id: Int,

    @Json(name = "createdAt")
    var createdAt: String,

    @Json(name = "body")
    var body: String,

    @Json(name = "author")
    var author: Author,

    ) : Parcelable