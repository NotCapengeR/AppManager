package ru.netology.app_manager.core.api.models

import android.os.Parcelable
import android.os.VibrationAttributes
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.netology.app_manager.core.apk.manager.AnalysisMap


@Serializable
@Parcelize
data class AnalysisResponse(
    val meta: Meta? = null,
    val data: Data
): Parcelable {

    @Parcelize
    @Serializable
    data class Meta(
        @SerialName("file_info")
        val fileInfo: FileInfo
    ): Parcelable {
        @Serializable
        @Parcelize
        data class FileInfo(
            val sha256: String,
            val sha1: String,
            val md5: String,
            val size: Long
        ) : Parcelable
    }

    @Parcelize
    @Serializable
    data class Data(
        val attributes: Attributes
    ) : Parcelable {

        @Parcelize
        @Serializable
        data class Attributes(
            val date: Long,
            val status: String,
            val stats: Stats,
            val results: AnalysisMap
        ) : Parcelable

        @Parcelize
        @Serializable
        data class Stats(
            val harmless: Int,
            @SerialName("type-unsupported")
            val typeUnsupported: Int,
            val suspicious: Int,
            @SerialName("confirmed-timeout")
            val confirmedTimeout: Int,
            val timeout: Int,
            val failure: Int,
            val malicious: Int,
            val undetected: Int,
        ) : Parcelable
    }
}