package xyz.harmonyapp.olympusblog.persistence

import androidx.lifecycle.LiveData
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
    fun searchById(id: Int): LiveData<AccountProperties>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAndReplace(accountProperties: AccountProperties): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(accountProperties: AccountProperties): Long

    @Query("UPDATE account_properties SET email = :email, username = :username, bio = :bio, image = :image WHERE id = :id")
    fun updateAccountProperties(id: Int, email: String, username: String, bio: String, image: String)
}