package ru.netology.app_manager.di

import android.content.Context
import androidx.fragment.app.Fragment
import dagger.Component
import ru.netology.app_manager.App
import ru.netology.app_manager.di.modules.AppModule
import ru.netology.app_manager.di.modules.MemoryModule
import ru.netology.app_manager.di.modules.NetworkModule
import ru.netology.app_manager.di.modules.ViewModelModule
import ru.netology.app_manager.ui.MainActivity
import ru.netology.app_manager.ui.activities_list.ActivitiesListAppFragment
import ru.netology.app_manager.ui.activities_list.ActivityListFragment
import ru.netology.app_manager.ui.app_list.AppDetailsFragment
import ru.netology.app_manager.ui.app_list.MainFragment
import ru.netology.app_manager.ui.backup.BackupFragment
import ru.netology.app_manager.ui.backup.LoginFragment
import ru.netology.app_manager.ui.virustotal.VirusTotalFragment
import javax.inject.Singleton

@Singleton
@Component(modules = [MemoryModule::class, ViewModelModule::class, NetworkModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {

        fun appModule(appModule: AppModule): Builder

        fun build(): AppComponent
    }

    fun inject(application: App)
    fun inject(activity: MainActivity)
    fun inject(fragment: MainFragment)
    fun inject(fragment: BackupFragment)
    fun inject(fragment: LoginFragment)
    fun inject(fragment: AppDetailsFragment)
    fun inject(fragment: VirusTotalFragment)
    fun inject(fragment: ActivityListFragment)
    fun inject(fragment: ActivitiesListAppFragment)
}

fun Fragment.getAppComponent(): AppComponent =
    (requireContext().applicationContext as App).appComponent

fun Context.getAppComponent(): AppComponent = when (this) {
    is App -> appComponent
    else -> (applicationContext as App).appComponent
}