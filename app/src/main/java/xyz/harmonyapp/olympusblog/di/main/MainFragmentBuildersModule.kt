package xyz.harmonyapp.olympusblog.di.main

import dagger.Module
import dagger.android.ContributesAndroidInjector
import xyz.harmonyapp.olympusblog.ui.main.account.AccountFragment
import xyz.harmonyapp.olympusblog.ui.main.account.ChangePasswordFragment
import xyz.harmonyapp.olympusblog.ui.main.account.UpdateAccountFragment
import xyz.harmonyapp.olympusblog.ui.main.article.ArticleFragment
import xyz.harmonyapp.olympusblog.ui.main.article.UpdateArticleFragment
import xyz.harmonyapp.olympusblog.ui.main.article.ViewArticleFragment
import xyz.harmonyapp.olympusblog.ui.main.create.CreateArticleFragment

@Module
abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeArticleFragment(): ArticleFragment

    @ContributesAndroidInjector()
    abstract fun contributeAccountFragment(): AccountFragment

    @ContributesAndroidInjector()
    abstract fun contributeChangePasswordFragment(): ChangePasswordFragment

    @ContributesAndroidInjector()
    abstract fun contributeCreateArticleFragment(): CreateArticleFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateArticleFragment(): UpdateArticleFragment

    @ContributesAndroidInjector()
    abstract fun contributeViewArticleFragment(): ViewArticleFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateAccountFragment(): UpdateAccountFragment
}