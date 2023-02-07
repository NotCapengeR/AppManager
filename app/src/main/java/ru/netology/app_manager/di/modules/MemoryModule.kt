package ru.netology.app_manager.di.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.netology.app_manager.App
import ru.netology.app_manager.core.api.repository.BackendRepository
import ru.netology.app_manager.core.api.repository.BackendRepositoryImpl
import ru.netology.app_manager.core.apk.manager.ApkRepository
import ru.netology.app_manager.core.apk.manager.ApkRepositoryImp
import ru.netology.app_manager.core.db.AppDatabase
import ru.netology.app_manager.core.db.dao.BackupDao
import ru.netology.app_manager.core.helper.prefs.PrefsManager.Companion.SHARED_PREFS_NAME
import javax.inject.Singleton

@Module(includes = [AppModule::class])
class MemoryModule {

    @Provides
    @Singleton
    fun provideSharedPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)


    @Provides
    @Singleton
    fun provideAppDb(context: Context): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        AppDatabase.DB_NAME
    ).fallbackToDestructiveMigration().build()


    @Provides
    @Singleton
    fun provideBackupDao(db: AppDatabase): BackupDao = db.getBackupDao()

}

@Module(includes = [RepositoryBinder::class])
class AppModule(private val application: App) {

    @Provides
    @Singleton
    fun provideContext(): Context = application

    @Provides
    @Singleton
    fun provideApp(): Application = application
}

@Module
interface RepositoryBinder {

    @Binds
    @Singleton
    fun bindApkRepository(repository: ApkRepositoryImp): ApkRepository

    @Binds
    @Singleton
    fun bindBackendRepository(repository: BackendRepositoryImpl): BackendRepository
}
