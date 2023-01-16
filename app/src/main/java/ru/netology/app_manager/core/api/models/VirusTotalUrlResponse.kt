package ru.netology.app_manager.core.api.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class VirusTotalUrlResponse(
    val data: String
): Parcelable