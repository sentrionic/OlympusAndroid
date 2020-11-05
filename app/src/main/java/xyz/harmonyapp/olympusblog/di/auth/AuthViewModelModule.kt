package xyz.harmonyapp.olympusblog.di.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import xyz.harmonyapp.olympusblog.di.auth.keys.AuthViewModelKey
import xyz.harmonyapp.olympusblog.ui.auth.AuthViewModel
import xyz.harmonyapp.olympusblog.viewmodels.AuthViewModelFactory

@Module
abstract class AuthViewModelModule {

    @AuthScope
    @Binds
    abstract fun bindViewModelFactory(factory: AuthViewModelFactory): ViewModelProvider.Factory

    @AuthScope
    @Binds
    @IntoMap
    @AuthViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

}