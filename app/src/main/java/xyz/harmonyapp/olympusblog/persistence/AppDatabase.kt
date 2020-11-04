package xyz.harmonyapp.olympusblog.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import xyz.harmonyapp.olympusblog.models.AccountProperties
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.models.AuthToken

@Database(entities = [AuthToken::class, AccountProperties::class, Article::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    abstract fun getArticlesDao(): ArticlesDao

    companion object{
        val DATABASE_NAME: String = "app_db"
    }
}