package ru.netology.app_manager.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.app_manager.core.db.AppDatabase.Companion.DB_VERSION
import ru.netology.app_manager.core.db.dao.BackupDao
import ru.netology.app_manager.core.db.entities.BackupEntity

@Database(
    entities = [BackupEntity::class],
    version = DB_VERSION,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getBackupDao(): BackupDao

    companion object {
        const val DB_VERSION: Int = 1
        const val DB_NAME: String = "app-manager-database"
    }
}