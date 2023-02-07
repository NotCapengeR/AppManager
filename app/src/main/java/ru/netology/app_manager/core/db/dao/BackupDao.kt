package ru.netology.app_manager.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import ru.netology.app_manager.core.db.entities.BackupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupDao {

    @Query("SELECT * FROM backups WHERE backup_id > 0 ORDER BY backup_id DESC")
    fun getAll(): Flow<List<BackupEntity>>

    @Query("SELECT COUNT(*) FROM backups")
    suspend fun getSize(): Int

    @Query("DELETE FROM backups")
    suspend fun removeAll(): Int

    @Query("SELECT * FROM backups WHERE backup_id = :id LIMIT 1")
    suspend fun getById(id: Long): BackupEntity?

    @Query("DELETE FROM backups WHERE backup_id = :id")
    suspend fun deleteById(id: Long): Int

    @Insert(onConflict = REPLACE)
    suspend fun insert(backup: BackupEntity): Long

    @Insert(onConflict = REPLACE)
    suspend fun insertAll(vararg backups: BackupEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertAll(backups: List<BackupEntity>)
}