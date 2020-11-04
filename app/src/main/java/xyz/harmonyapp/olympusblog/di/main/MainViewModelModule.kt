package xyz.harmonyapp.olympusblog.di.main

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import xyz.harmonyapp.olympusblog.di.ViewModelKey
import xyz.harmonyapp.olympusblog.ui.main.account.AccountViewModel
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.ArticleViewModel
import xyz.harmonyapp.olympusblog.ui.main.create.CreateArticleViewModel

@Module
abstract class MainViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accoutViewModel: AccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ArticleViewModel::class)
    abstract fun bindArticleViewModel(articleViewModel: ArticleViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateArticleViewModel::class)
    abstract fun bindCreateArticleViewModel(createArticleViewModel: CreateArticleViewModel): ViewModel
}