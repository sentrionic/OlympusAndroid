package xyz.harmonyapp.olympusblog.di.main

import android.app.Application
import dagger.Module
import dagger.Provides
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import retrofit2.Retrofit
import xyz.harmonyapp.olympusblog.api.main.MainService
import xyz.harmonyapp.olympusblog.persistence.AccountPropertiesDao
import xyz.harmonyapp.olympusblog.persistence.AppDatabase
import xyz.harmonyapp.olympusblog.persistence.ArticlesDao
import xyz.harmonyapp.olympusblog.repository.main.AccountRepository
import xyz.harmonyapp.olympusblog.repository.main.ArticleRepository
import xyz.harmonyapp.olympusblog.repository.main.CreateArticleRepository
import xyz.harmonyapp.olympusblog.session.SessionManager

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideOpenApiMainService(retrofitBuilder: Retrofit.Builder): MainService {
        return retrofitBuilder
            .build()
            .create(MainService::class.java)
    }

    @MainScope
    @Provides
    fun provideAccountRepository(
        mainService: MainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepository(mainService, accountPropertiesDao, sessionManager)
    }

    @MainScope
    @Provides
    fun provideArticlesDao(db: AppDatabase): ArticlesDao {
        return db.getArticlesDao()
    }

    @MainScope
    @Provides
    fun provideMarkwon(application: Application): Markwon {
        return Markwon.create(application.applicationContext)
    }

    @MainScope
    @Provides
    fun provideMarkwonEditor(markwon: Markwon): MarkwonEditor {
        return MarkwonEditor.create(markwon)
    }

    @MainScope
    @Provides
    fun provideArticleRepository(
        mainService: MainService,
        articlesDao: ArticlesDao,
        sessionManager: SessionManager
    ): ArticleRepository {
        return ArticleRepository(mainService, articlesDao, sessionManager)
    }

    @MainScope
    @Provides
    fun provideCreateArticleRepository(
        mainService: MainService,
        articlesDao: ArticlesDao,
        sessionManager: SessionManager
    ): CreateArticleRepository {
        return CreateArticleRepository(mainService, articlesDao, sessionManager)
    }
}