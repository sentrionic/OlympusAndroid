package xyz.harmonyapp.olympusblog.di.main

import dagger.Subcomponent
import xyz.harmonyapp.olympusblog.ui.main.MainActivity

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