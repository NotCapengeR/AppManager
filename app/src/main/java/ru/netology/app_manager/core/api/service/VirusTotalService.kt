package ru.netology.app_manager.core.api.service

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.app_manager.core.api.models.AnalysisResponse
import ru.netology.app_manager.core.api.models.UploadResponse
import ru.netology.app_manager.core.api.models.VirusTotalFileResponse
import ru.netology.app_manager.core.api.models.VirusTotalUrlResponse

interface VirusTotalService {

    companion object {
        const val BASE_URL: String = "https://www.virustotal.com/api/v3/"
        const val TOKEN: String = "320285f05f5deb8c23ecba8ab8826edda775c426a6aaa122708d3556400e5dab"
    }

    @GET("files/{hash}")
    suspend fun getFileByHash(
        @Path("hash") hash: String,
        @Header("x-apikey") key: String
    ): Response<VirusTotalFileResponse>

    @POST("files")
    @Headers("Content-Type: multipart/form-data", "Accept: application/json")
    @Multipart
    suspend fun simpleUpload(
        @Header("x-apikey") token: String,
        @Part file: MultipartBody.Part,
    ): Response<UploadResponse>

    @GET("files/upload_url")
    suspend fun getUploadUrlForBigFiles(
        @Header("x-apikey") token: String
    ): Response<VirusTotalUrlResponse>

    @POST
    @Headers("Content-Type: multipart/form-data", "Accept: application/json")
    @Multipart
    suspend fun uploadBigFile(
        @Url url: String,
        @Header("x-apikey") token: String,
        @Part file: MultipartBody.Part,
    ): Response<UploadResponse>

    @GET("analyses/{id}")
    suspend fun getAnalysisResult(
        @Header("x-apikey") token: String,
        @Path("id") id: String
    ): Response<AnalysisResponse>
}