package xyz.harmonyapp.olympusblog.di.auth

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import xyz.harmonyapp.olympusblog.api.auth.AuthService
import xyz.harmonyapp.olympusblog.persistence.AccountPropertiesDao
import xyz.harmonyapp.olympusblog.persistence.AuthTokenDao
import xyz.harmonyapp.olympusblog.repository.auth.AuthRepository
import xyz.harmonyapp.olympusblog.repository.auth.AuthRepositoryImpl
import xyz.harmonyapp.olympusblog.session.SessionManager

@Module
object AuthModule {

    @JvmStatic
    @AuthScope
    @Provides
    fun provideAuthService(retrofitBuilder: Retrofit.Builder): AuthService {
        return retrofitBuilder
            .build()
            .create(AuthService::class.java)
    }

    @JvmStatic
    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        authService: AuthService,
        preferences: SharedPreferences,
        editor: SharedPreferences.Editor
    ): AuthRepository {
        return AuthRepositoryImpl(
            authTokenDao,
            accountPropertiesDao,
            authService,
            sessionManager,
            preferences,
            editor
        )
    }

}