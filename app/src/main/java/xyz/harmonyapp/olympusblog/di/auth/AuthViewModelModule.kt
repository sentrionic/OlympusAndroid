package xyz.harmonyapp.olympusblog.di.auth

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import xyz.harmonyapp.olympusblog.di.ViewModelKey
import xyz.harmonyapp.olympusblog.ui.auth.AuthViewModel

@Module
abstract class AuthViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

}