package ru.netology.app_manager

import android.app.Application
import ru.netology.app_manager.di.AppComponent
import ru.netology.app_manager.di.DaggerAppComponent
import ru.netology.app_manager.di.getAppComponent
import ru.netology.app_manager.di.modules.AppModule
import timber.log.Timber

class App : Application() {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        getAppComponent().inject(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}