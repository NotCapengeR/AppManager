package ru.netology.app_manager.core.api.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class UploadResponse(
    val data: Data
): Parcelable {

    @Parcelize
    @Serializable
    data class Data(
        val type: String,
        val id: String
    ) : Parcelable
}