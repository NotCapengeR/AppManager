package ru.netology.app_manager.core.apk.manager

import ru.netology.app_manager.core.api.models.VirusTotalFileResponse
import ru.netology.app_manager.core.api.service.VirusTotalService
import ru.netology.app_manager.core.api.service.VirusTotalService.Companion.TOKEN
import ru.netology.app_manager.core.apk.models.VirusTotalFile
import ru.netology.app_manager.core.helper.exceptions.ExceptionProvider
import ru.netology.app_manager.core.helper.network.NetworkResult
import ru.netology.app_manager.core.helper.network.safeApiCall
import ru.netology.app_manager.utils.StringUtils.asMultipart
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

interface ApkRepository {
    suspend fun getFileByHash(hash: String): VirusTotalFile?

    suspend fun uploadFile(file: File, name: String): String?

    suspend fun getAnalysisById(id: String): AnalysisMap?
}

typealias AnalysisMap = Map<String, VirusTotalFileResponse.AnalysisResult>

@Singleton
class ApkRepositoryImp @Inject constructor(
    private val exceptionProvider: ExceptionProvider,
    private val service: VirusTotalService
) : ApkRepository {

    override suspend fun getFileByHash(hash: String): VirusTotalFile? {
        val result = safeApiCall { service.getFileByHash(hash, TOKEN) }
        exceptionProvider.setLastError(result)
        if (result is NetworkResult.Success) {
            return VirusTotalFile.fromResponse(result.data)
        }
        return null
    }

    override suspend fun uploadFile(file: File, name: String): String? {
        if (file.length() <= VIRUS_TOTAL_SIMPLE_LIMIT) {
            val result = safeApiCall {
                service.simpleUpload(
                    TOKEN,
                    file.asMultipart(FILE_FIELD_NAME, name, APK_MIME_TYPE)
                )
            }
            exceptionProvider.setLastError(result)
            if (result is NetworkResult.Success) {
                return result.data.data.id
            }
        } else {
            val urlResult = safeApiCall { service.getUploadUrlForBigFiles(TOKEN) }
            val url = urlResult.data?.data ?: return null.also {
                exceptionProvider.setLastError(urlResult)
            }
            val result =
                safeApiCall {
                    service.uploadBigFile(
                        url, TOKEN, file.asMultipart(FILE_FIELD_NAME, name, APK_MIME_TYPE)
                    )
                }
            exceptionProvider.setLastError(result)
            if (result is NetworkResult.Success) {
                return result.data.data.id
            }
        }
        return null
    }

    override suspend fun getAnalysisById(id: String): AnalysisMap? {
        return safeApiCall { service.getAnalysisResult(TOKEN, id) }.data?.data?.attributes?.results
    }

    private companion object {
        private const val VIRUS_TOTAL_SIMPLE_LIMIT: Int = 33554432
        private const val APK_MIME_TYPE: String = "application/vnd.android.package-archive"
        private const val FILE_FIELD_NAME: String = "file"
    }
}