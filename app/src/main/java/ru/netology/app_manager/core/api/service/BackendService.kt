package ru.netology.app_manager.core.api.service

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.app_manager.core.api.models.Backup
import ru.netology.app_manager.core.api.models.LoginResponse
import ru.netology.app_manager.core.api.models.Message
import ru.netology.app_manager.core.api.models.User

interface BackendService {

    companion object {
        const val BASE_URL = "http://10.0.2.2:9999/api/"
    }


    @FormUrlEncoded
    @POST("users/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @Headers("Accept: application/json")
    @GET("users/login")
    suspend fun getUser(): Response<User>


    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("users/register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>


    @GET("backups")
    @Headers("Accept: application/json")
    suspend fun getBackups(): Response<List<Backup>>

    @POST("backups")
    @Multipart
    suspend fun newBackup(
        @Part file: MultipartBody.Part,
        @Part("comment") comment: String? = null,
    ): Response<Backup>


    @DELETE("backups/{backup_id}")
    @Headers("Accept: application/json")
    suspend fun deleteBackup(@Path("backup_id") backupId: Long): Response<Message>

    @GET("backups/{backup_id}")
    @Headers("Accept: application/json")
    suspend fun getBackupInfo(@Path("backup_id") backupId: Long): Response<Backup>

    @GET("backups/{backup_id}/download")
    @Streaming
    @Headers("Accept: application/zip; application/octet-stream", "Accept-Encoding: gzip, identity")
    suspend fun downloadBackup(@Path("backup_id") backupId: Long): Response<ResponseBody>

}