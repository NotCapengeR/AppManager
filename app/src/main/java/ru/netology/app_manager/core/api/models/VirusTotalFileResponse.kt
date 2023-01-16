package ru.netology.app_manager.core.api.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.netology.app_manager.core.apk.models.VirusTotalFile

@Parcelize
@Serializable
data class VirusTotalFileResponse(
    val data: Data
): Parcelable {

    @Parcelize
    @Serializable
    data class Data(
        val attributes: VirusTotalFile
    ) : Parcelable

    @Parcelize
    @Serializable
    data class AnalysisResult(
        val category: String,
        @SerialName("engine_name")
        val engineName: String,
        @SerialName("engine_update")
        val engineUpdate: String,
        @SerialName("engine_version")
        val engineVersion: String? = null,
        val method: String,
        val result: String? = null
    ) : Parcelable

    @Parcelize
    @Serializable
    data class TotalVotes(val harmless: Int, val malicious: Int) : Parcelable
}