package xyz.harmonyapp.olympusblog.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "account_properties")
data class AccountProperties(
    @Json(name = "id")
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id") var id: Int,

    @Json(name = "email")
    @ColumnInfo(name = "email") var email: String,

    @Json(name = "username")
    @ColumnInfo(name = "username") var username: String,

    @Json(name = "bio")
    @ColumnInfo(name = "bio") var bio: String,

    @Json(name = "image")
    @ColumnInfo(name = "image") var image: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountProperties

        if (id != other.id) return false
        if (email != other.email) return false
        if (username != other.username) return false
        if (bio != other.bio) return false
        if (image != other.image) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + email.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + bio.hashCode()
        result = 31 * result + image.hashCode()
        return result
    }
}