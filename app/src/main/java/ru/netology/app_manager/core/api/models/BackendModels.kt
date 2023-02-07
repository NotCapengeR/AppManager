package ru.netology.app_manager.core.api.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.netology.app_manager.core.db.entities.BackupEntity


@Serializable
@Parcelize
data class Message(
    val message: String
) : Parcelable


@Serializable
@Parcelize
data class User(
    @SerialName("user_id") val id: Long,
    @SerialName("username") val username: String,
    @SerialName("joined") val joined: String,
) : Parcelable

@Serializable
@Parcelize
data class LoginResponse(
    val token: String,
    val user: User,
) : Parcelable


@Serializable
@Parcelize
data class Backup(
    @SerialName("backup_id") val id: Long,
    @SerialName("user_id") val userId: Long,
    @SerialName("username") val username: String,
    @SerialName("comment") val comment: String?,
    @SerialName("created") val created: String,
) : Parcelable {

    fun toEntity(): BackupEntity = BackupEntity(
        id = id,
        userId = userId,
        comment = comment,
        username = username,
        created = created
    )

    companion object {
        fun fromEntity(entity: BackupEntity) = Backup(
            id = entity.id,
            userId = entity.userId,
            comment = entity.comment,
            username = entity.username,
            created = entity.created
        )

        fun fromEntity(entities: List<BackupEntity>) = entities.map {
            fromEntity(it)
        }
    }
}
