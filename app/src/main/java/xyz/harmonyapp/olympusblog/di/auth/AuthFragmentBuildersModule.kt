package xyz.harmonyapp.olympusblog.di.auth

import dagger.Module
import dagger.android.ContributesAndroidInjector
import xyz.harmonyapp.olympusblog.ui.auth.ForgotPasswordFragment
import xyz.harmonyapp.olympusblog.ui.auth.LauncherFragment
import xyz.harmonyapp.olympusblog.ui.auth.RegisterFragment

@Module
abstract class AuthFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeLauncherFragment(): LauncherFragment

    @ContributesAndroidInjector()
    abstract fun contributeRegisterFragment(): RegisterFragment

    @ContributesAndroidInjector()
    abstract fun contributeForgotPasswordFragment(): ForgotPasswordFragment

}