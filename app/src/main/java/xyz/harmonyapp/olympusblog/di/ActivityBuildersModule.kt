package xyz.harmonyapp.olympusblog.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import xyz.harmonyapp.olympusblog.di.auth.AuthFragmentBuildersModule
import xyz.harmonyapp.olympusblog.di.auth.AuthModule
import xyz.harmonyapp.olympusblog.di.auth.AuthScope
import xyz.harmonyapp.olympusblog.di.auth.AuthViewModelModule
import xyz.harmonyapp.olympusblog.di.main.MainFragmentBuildersModule
import xyz.harmonyapp.olympusblog.di.main.MainModule
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.di.main.MainViewModelModule
import xyz.harmonyapp.olympusblog.ui.auth.AuthActivity
import xyz.harmonyapp.olympusblog.ui.main.MainActivity

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthModule::class, AuthFragmentBuildersModule::class, AuthViewModelModule::class]
    )
    abstract fun contributeAuthActivity(): AuthActivity

    @MainScope
    @ContributesAndroidInjector(
        modules = [MainModule::class, MainFragmentBuildersModule::class, MainViewModelModule::class]
    )
    abstract fun contributeMainActivity(): MainActivity

}