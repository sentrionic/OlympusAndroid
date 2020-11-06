package xyz.harmonyapp.olympusblog.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xyz.harmonyapp.olympusblog.models.AuthToken

@Dao
interface AuthTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(authToken: AuthToken): Long

    @Query("UPDATE auth_token SET token = null WHERE account_id = :id")
    suspend fun nullifyToken(id: Int): Int

    @Query("SELECT * FROM auth_token WHERE account_id = :id")
    suspend fun searchById(id: Int): AuthToken?
}