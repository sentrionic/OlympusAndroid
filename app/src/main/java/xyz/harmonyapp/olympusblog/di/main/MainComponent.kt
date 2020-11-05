package xyz.harmonyapp.olympusblog.di.main

import dagger.Subcomponent
import xyz.harmonyapp.olympusblog.ui.main.MainActivity
import xyz.harmonyapp.olympusblog.ui.main.account.BaseAccountFragment
import xyz.harmonyapp.olympusblog.ui.main.article.BaseArticleFragment
import xyz.harmonyapp.olympusblog.ui.main.create.BaseCreateArticleFragment

@MainScope
@Subcomponent(
    modules = [
        MainModule::class,
        MainViewModelModule::class,
        MainFragmentsModule::class
    ]
)
interface MainComponent {

    @Subcomponent.Factory
    interface Factory {

        fun create(): MainComponent
    }

    fun inject(mainActivity: MainActivity)

}