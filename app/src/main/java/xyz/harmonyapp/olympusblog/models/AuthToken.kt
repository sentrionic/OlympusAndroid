package xyz.harmonyapp.olympusblog.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

const val AUTH_TOKEN_BUNDLE_KEY = "xyz.harmonyapp.olympusblog.models.AuthToken"

@Parcelize
@Entity(
    tableName = "auth_token",
    foreignKeys = [
        ForeignKey(
            entity = AccountProperties::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = CASCADE
        )
    ]
)
data class AuthToken(

    @PrimaryKey
    @ColumnInfo(name = "account_id")
    var account_id: Int? = -1,

    @ColumnInfo(name = "token")
    @Json(name = "token")
    var token: String? = null
) : Parcelable