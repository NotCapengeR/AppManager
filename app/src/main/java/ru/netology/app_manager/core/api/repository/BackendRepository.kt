package ru.netology.app_manager.core.api.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.HttpException
import retrofit2.Response
import ru.netology.app_manager.core.api.models.Backup
import ru.netology.app_manager.core.api.models.LoginResponse
import ru.netology.app_manager.core.api.service.BackendService
import ru.netology.app_manager.core.apk.manager.ApkExtractor
import ru.netology.app_manager.core.db.dao.BackupDao
import ru.netology.app_manager.core.helper.exceptions.ExceptionProvider
import ru.netology.app_manager.core.helper.network.NetworkResult
import ru.netology.app_manager.core.helper.network.safeApiCall
import ru.netology.app_manager.core.helper.prefs.PrefsManager
import ru.netology.app_manager.utils.NetworkUtils.saveAsFile
import ru.netology.app_manager.utils.StringUtils.asMultipart
import ru.netology.app_manager.utils.StringUtils.base64decode
import ru.netology.app_manager.utils.getErrorMessage
import timber.log.Timber
import java.io.File
import java.net.ProtocolException
import javax.inject.Inject
import javax.inject.Singleton

interface BackendRepository {

    val isLoggedIn: Boolean
    val token: Flow<String?>

    suspend fun login(username: String, password: String): LoginResponse?
    suspend fun register(username: String, password: String): LoginResponse?

    suspend fun newBackup(file: File, comment: String? = null): Boolean
    suspend fun deleteBackup(backupId: Long)


    fun getBackups(): Flow<List<Backup>>

    suspend fun fetchData()


    suspend fun signOut()
    suspend fun downloadBackup(id: Long): File?
}


@Singleton
class BackendRepositoryImpl @Inject constructor(
    private val exceptionProvider: ExceptionProvider,
    private val service: BackendService,
    private val prefs: PrefsManager,
    private val auth: AuthManager,
    private val dao: BackupDao,
    private val context: Context
) : BackendRepository {


    override val isLoggedIn: Boolean
        get() = auth.token.value != null
    override val token: Flow<String?> = auth.token

    override suspend fun fetchData() {
        val result = safeApiCall { service.getBackups() }
        if (result is NetworkResult.Success) {
            dao.insertAll(result.data.map { backup -> backup.toEntity() })
        }
    }

    override fun getBackups(): Flow<List<Backup>> = dao.getAll()
        .map(Backup::fromEntity)
        .catch { Timber.e(it.getErrorMessage()) }
        .flowOn(Dispatchers.IO)

    override suspend fun signOut() {
        prefs.putString(PrefsManager.TOKEN_KEY, null)
        auth.setToken(null)
        dao.removeAll()
    }

    override suspend fun downloadBackup(id: Long): File? {
        try {
            val downloadManager: DownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val url = "${BackendService.BASE_URL}backups/$id/download"
            val downloadUrl = Uri.parse(url)
            val request = DownloadManager.Request(downloadUrl)

            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setAllowedOverRoaming(false)
            request.setTitle("Backup $id")
            request.addRequestHeader("Authorization", token.first())
            request.setDescription("Downloading Your Backups")
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Backup-$id.zip")

            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

    //Enqueue a new download and same the referenceId
            downloadManager.enqueue(request)
        } catch (ex: Throwable) {
            Timber.e(ex.getErrorMessage())
        }
        return null
    }


    private suspend fun <T> callWithLastError(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        apiCall: suspend () -> Response<T>
    ): NetworkResult<T> {
        return safeApiCall(dispatcher, apiCall).also(exceptionProvider::setLastError)
    }


    override suspend fun register(username: String, password: String): LoginResponse? {
        val result = callWithLastError {
            service.register(
                username,
                password
            )
        }
        return result.data.also {
            val token = it?.token
            prefs.putString(PrefsManager.TOKEN_KEY, token)
            auth.setToken(token)
        }
    }

    override suspend fun login(username: String, password: String): LoginResponse? {
        val result = callWithLastError { service.login(username, password) }
        return result.data.also {
            val token = it?.token
            prefs.putString(PrefsManager.TOKEN_KEY, token)
            auth.setToken(token)
        }
    }

    override suspend fun newBackup(file: File, comment: String?): Boolean {
        val result = callWithLastError {
            service.newBackup(
                file.asMultipart("file", contentType = "application/x-zip"),
                comment
            )
        }
        if (result is NetworkResult.Success) {
            dao.insert(result.data.toEntity())
        }
        return result.data != null
    }

    override suspend fun deleteBackup(backupId: Long) {
        if (safeApiCall { service.deleteBackup(backupId) } is NetworkResult.Success) {
            dao.deleteById(backupId)
        }
    }
}