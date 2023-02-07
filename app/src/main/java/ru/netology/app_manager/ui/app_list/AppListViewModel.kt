package ru.netology.app_manager.ui.app_list

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.zeroturnaround.zip.ZipUtil
import ru.netology.app_manager.core.api.models.Backup
import ru.netology.app_manager.core.api.repository.BackendRepository
import ru.netology.app_manager.core.apk.manager.ApkExtractor
import ru.netology.app_manager.core.apk.models.Apk
import ru.netology.app_manager.core.helper.exceptions.ExceptionProvider
import ru.netology.app_manager.core.helper.exceptions.NoError
import ru.netology.app_manager.utils.getErrorMessage
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class AppListViewModel @Inject constructor(
    application: Application,
    private val repository: BackendRepository,
    private val exceptionProvider: ExceptionProvider
) : AndroidViewModel(application) {

    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoggedIn: Boolean
        get() = repository.isLoggedIn
    val appList: MutableLiveData<List<Apk>> = MutableLiveData(emptyList())
    val backups: LiveData<List<Backup>> = repository.getBackups().asLiveData()
    val onSuccess: MutableLiveData<String?> = MutableLiveData(null)
    val error: MutableLiveData<String?> = MutableLiveData(null)

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            isLoading.value = true
            val metadataList: List<PackageInfo> = getAllAppsMetaData()
            appList.value = metadataList.map { info: PackageInfo ->
                Apk.fromPackageInfo(info, getApplication())
            }
            isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.signOut()
        }
    }

    fun extractAll(comment: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)
            val appPath = ApkExtractor.getAppFolder()
            val appsList = mutableListOf<File?>()
            appList.value?.filterNot { it.isSystem }?.forEach { apk ->
                try {
                    ApkExtractor.extractApk(
                        apk = apk,
                        onSuccess = {
                            val file = ApkExtractor.getApkFile(apk)
                            Timber.d(
                                "App ${apk.appName} was successfully extracted to ${
                                    file?.path
                                }"
                            )
                            appsList.add(file)
                        },
                        onError = { Timber.e("Error: Could not extract app ${apk.appName}") }
                    )
                } catch (ex: SecurityException) {
                    Timber.e("Error occurred: ${ex.getErrorMessage()}")
                    return@forEach
                }
            }
            if (appPath != null) {
                val zip = File(appPath, "Backup.zip")
                ZipUtil.createEmpty(zip)
                ZipUtil.packEntries(appsList.filterNotNull().toTypedArray(), zip)
                repository.newBackup(zip, comment)
            }
            isLoading.postValue(false)
        }
    }

    fun deleteBackup(backupId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBackup(backupId)
        }
    }

    fun installBackup(backupId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)
            val backup = repository.downloadBackup(backupId)
            if (backup != null) {
                onSuccess.value = backup.absolutePath
            } else {
                withContext(Dispatchers.Main) {
                    exceptionProvider.getLastError().error.apply {
                        if (this != NoError) error.value = this.getErrorMessage()
                    }
                }
            }
            isLoading.postValue(false)
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            repository.fetchData()
        }
    }

    private fun getAllAppsMetaData(): List<PackageInfo> =
        ApkExtractor.getAllApps(getApplication(), PackageManager.GET_META_DATA)
}