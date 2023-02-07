package ru.netology.app_manager.core.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "backups",
    indices = [Index("backup_id")]
)
data class BackupEntity(
    @PrimaryKey
    @ColumnInfo(name = "backup_id")
    val id: Long,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "comment")
    val comment: String?,
    @ColumnInfo(name = "created")
    val created: String,
)