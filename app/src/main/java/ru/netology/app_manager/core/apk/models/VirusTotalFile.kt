package ru.netology.app_manager.core.apk.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.netology.app_manager.core.api.models.VirusTotalFileResponse
import ru.netology.app_manager.core.apk.manager.AnalysisMap

@Parcelize
@Serializable
data class VirusTotalFile(
    @SerialName("creation_date")
    val creationDate: Long? = null,
    @SerialName("size")
    val size: Long,
    @SerialName("vhash")
    val vhash: String,
    @SerialName("tlsh")
    val tlsh: String? = null,
    @SerialName("first_submission_date")
    val firstSubmissionDate: Long,
    @SerialName("last_analysis_date")
    val lastAnalysisDate: Long,
    @SerialName("md5")
    val md5: String,
    @SerialName("sha256")
    val sha256: String,
    @SerialName("sha1")
    val sha1: String,
    @SerialName("tags")
    val tags: List<String>,
    @SerialName("names")
    val names: List<String>,
    @SerialName("meaningful_name")
    val meaningfulName: String? = null,
    @SerialName("type_description")
    val typeDescription: String,
    @SerialName("type_extension")
    val typeExtension: String,
    @SerialName("type_tag")
    val typeTag: String,
    @SerialName("total_votes")
    val totalVotes: VirusTotalFileResponse.TotalVotes,
    @SerialName("times_submitted")
    val timesSubmitted: Int,
    @SerialName("last_analysis_results")
    val lastAnalysisResults: AnalysisMap,
) : Parcelable {
    companion object {
        fun fromResponse(response: VirusTotalFileResponse): VirusTotalFile = response.data.attributes
    }
}