package ru.netology.app_manager.core.api.repository

import ru.netology.app_manager.core.api.models.LoginResponse
import ru.netology.app_manager.core.api.service.BackendService
import ru.netology.app_manager.core.helper.network.safeApiCall
import ru.netology.app_manager.core.helper.prefs.PrefsManager
import ru.netology.app_manager.utils.StringUtils.asMultipart
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

interface BackendRepository {

    suspend fun login(username: String, password: String): LoginResponse?
    suspend fun register(username: String, password: String): LoginResponse?


    suspend fun newBackup(file: File, comment: String? = null): Boolean
    suspend fun isLoggedIn(): Boolean
}


@Singleton
class BackendRepositoryImpl @Inject constructor(
    private val service: BackendService,
    private val prefs: PrefsManager,
    private val auth: AuthManager
) : BackendRepository {

    override suspend fun isLoggedIn(): Boolean {
        return safeApiCall { service.getUser() }.data != null
    }

    override suspend fun register(username: String, password: String): LoginResponse? {
        return safeApiCall { service.register(username, password) }.data.also {
            val token = it?.token
            prefs.putString(PrefsManager.TOKEN_KEY, token)
            auth.setToken(token)
        }
    }

    override suspend fun login(username: String, password: String): LoginResponse? {
        return safeApiCall { service.login(username, password) }.data.also {
            val token = it?.token
            prefs.putString(PrefsManager.TOKEN_KEY, token)
            auth.setToken(token)
        }
    }

    override suspend fun newBackup(file: File, comment: String?): Boolean {
        return safeApiCall {
            service.newBackup(
                file.asMultipart("file", contentType = "application/x-zip"),
                comment
            )
        }.data != null
    }
}