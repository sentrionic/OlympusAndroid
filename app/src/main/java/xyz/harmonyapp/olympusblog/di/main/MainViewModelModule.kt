package xyz.harmonyapp.olympusblog.di.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import xyz.harmonyapp.olympusblog.di.main.keys.MainViewModelKey
import xyz.harmonyapp.olympusblog.ui.main.account.AccountViewModel
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.ArticleViewModel
import xyz.harmonyapp.olympusblog.ui.main.create.CreateArticleViewModel
import xyz.harmonyapp.olympusblog.viewmodels.MainViewModelFactory

@Module
abstract class MainViewModelModule {

    @MainScope
    @Binds
    abstract fun bindViewModelFactory(factory: MainViewModelFactory): ViewModelProvider.Factory

    @MainScope
    @Binds
    @IntoMap
    @MainViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accountViewModel: AccountViewModel): ViewModel

    @MainScope
    @Binds
    @IntoMap
    @MainViewModelKey(ArticleViewModel::class)
    abstract fun bindArticleViewModel(articleViewModel: ArticleViewModel): ViewModel

    @MainScope
    @Binds
    @IntoMap
    @MainViewModelKey(CreateArticleViewModel::class)
    abstract fun bindCreateArticleViewModel(createArticleViewModel: CreateArticleViewModel): ViewModel
}