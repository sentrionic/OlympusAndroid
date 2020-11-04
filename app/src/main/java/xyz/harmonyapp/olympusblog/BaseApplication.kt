package xyz.harmonyapp.olympusblog

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import xyz.harmonyapp.olympusblog.di.DaggerAppComponent

class BaseApplication: DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }
}