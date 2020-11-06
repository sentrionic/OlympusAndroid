package xyz.harmonyapp.olympusblog.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xyz.harmonyapp.olympusblog.models.AccountProperties

@Dao
interface AccountPropertiesDao {

    @Query("SELECT * FROM account_properties WHERE email = :email")
    suspend fun searchByEmail(email: String): AccountProperties?

    @Query("SELECT * FROM account_properties WHERE id = :id")
    suspend fun searchById(id: Int): AccountProperties

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAndReplace(accountProperties: AccountProperties): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(accountProperties: AccountProperties): Long

    @Query("UPDATE account_properties SET email = :email, username = :username, bio = :bio, image = :image WHERE id = :id")
    suspend fun updateAccountProperties(
        id: Int,
        email: String,
        username: String,
        bio: String,
        image: String
    )
}