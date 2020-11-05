package xyz.harmonyapp.olympusblog.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import xyz.harmonyapp.olympusblog.di.auth.AuthComponent
import xyz.harmonyapp.olympusblog.di.main.MainComponent
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.BaseActivity
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        SubComponentsModule::class
    ]
)
interface AppComponent {

    val sessionManager: SessionManager

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(baseActivity: BaseActivity)

    fun authComponent(): AuthComponent.Factory

    fun mainComponent(): MainComponent.Factory
}