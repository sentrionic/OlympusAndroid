package xyz.harmonyapp.olympusblog.di

import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import xyz.harmonyapp.olympusblog.viewmodels.ViewModelProviderFactory

@Module
abstract class ViewModelFactoryModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory
}