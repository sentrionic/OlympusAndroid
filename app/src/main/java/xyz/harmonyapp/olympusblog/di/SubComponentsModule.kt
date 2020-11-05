package xyz.harmonyapp.olympusblog.di

import dagger.Module
import xyz.harmonyapp.olympusblog.di.auth.AuthComponent
import xyz.harmonyapp.olympusblog.di.main.MainComponent

@Module(
    subcomponents = [
        AuthComponent::class,
        MainComponent::class
    ]
)
class SubComponentsModule