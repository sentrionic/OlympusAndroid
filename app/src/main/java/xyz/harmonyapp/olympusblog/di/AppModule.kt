package xyz.harmonyapp.olympusblog.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.persistence.AccountPropertiesDao
import xyz.harmonyapp.olympusblog.persistence.AppDatabase
import xyz.harmonyapp.olympusblog.persistence.AppDatabase.Companion.DATABASE_NAME
import xyz.harmonyapp.olympusblog.persistence.AuthTokenDao
import xyz.harmonyapp.olympusblog.utils.Constants
import xyz.harmonyapp.olympusblog.utils.LiveDataCallAdapterFactory
import xyz.harmonyapp.olympusblog.utils.PreferenceKeys
import javax.inject.Singleton

@Module
class AppModule {

    @Singleton
    @Provides
    fun provideSharedPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences(
            PreferenceKeys.APP_PREFERENCES,
            Context.MODE_PRIVATE
        )
    }

    @Singleton
    @Provides
    fun provideSharedPrefsEditor(sharedPreferences: SharedPreferences): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    @Singleton
    @Provides
    fun provideMoshiBuilder(): Moshi {
        return Moshi.Builder().build();
    }

    @Singleton
    @Provides
    fun provideCookieJar(application: Application): ClearableCookieJar {
        return PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(application))
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(cookieJar: ClearableCookieJar): OkHttpClient {
        return OkHttpClient
            .Builder()
            .cookieJar(cookieJar)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofitBuilder(moshiBuilder: Moshi, client: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .addConverterFactory(MoshiConverterFactory.create(moshiBuilder))
            .client(client)
    }


    @Singleton
    @Provides
    fun provideAppDb(app: Application): AppDatabase {
        return Room
            .databaseBuilder(app, AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration() // get correct db version if schema changed
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthTokenDao(db: AppDatabase): AuthTokenDao {
        return db.getAuthTokenDao()
    }

    @Singleton
    @Provides
    fun provideAccountPropertiesDao(db: AppDatabase): AccountPropertiesDao {
        return db.getAccountPropertiesDao()
    }

    @Singleton
    @Provides
    fun provideRequestOptions(): RequestOptions {
        return RequestOptions
            .placeholderOf(R.drawable.default_image)
            .error(R.drawable.default_image)
    }

    @Singleton
    @Provides
    fun provideGlideInstance(
        application: Application,
        requestOptions: RequestOptions
    ): RequestManager {
        return Glide.with(application)
            .setDefaultRequestOptions(requestOptions)
    }

}