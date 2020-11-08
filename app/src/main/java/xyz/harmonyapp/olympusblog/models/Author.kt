package xyz.harmonyapp.olympusblog.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "authors")
@Parcelize
data class Author(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    @Json(name = "id")
    var id: Int,

    @ColumnInfo(name = "username")
    @Json(name = "username")
    var username: String,

    @ColumnInfo(name = "bio")
    @Json(name = "bio")
    var bio: String,

    @ColumnInfo(name = "image")
    @Json(name = "image")
    var image: String,

    @ColumnInfo(name = "following")
    @Json(name = "following")
    var following: Boolean,

    @ColumnInfo(name = "followers")
    @Json(name = "followers")
    var followers: Int,

    @ColumnInfo(name = "followee")
    @Json(name = "followee")
    var followee: Int,
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Author

        if (id != other.id) return false
        if (username != other.username) return false
        if (bio != other.bio) return false
        if (image != other.image) return false
        if (following != other.following) return false
        if (followers != other.followers) return false
        if (followee != other.followee) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + username.hashCode()
        result = 31 * result + bio.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + following.hashCode()
        result = 31 * result + followers
        result = 31 * result + followee
        return result
    }

}