package ru.netology.app_manager.ui.virustotal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch
import ru.netology.app_manager.core.apk.manager.AnalysisMap
import ru.netology.app_manager.core.apk.manager.ApkExtractor
import ru.netology.app_manager.core.apk.manager.ApkRepository
import ru.netology.app_manager.core.apk.models.AppInfo
import ru.netology.app_manager.core.apk.models.VirusTotalFile
import ru.netology.app_manager.core.helper.exceptions.ExceptionProvider
import ru.netology.app_manager.core.helper.exceptions.LastError
import ru.netology.app_manager.utils.HashUtils.sha256
import timber.log.Timber
import java.io.File

class VirusTotalViewModel @AssistedInject constructor(
    @Assisted("apkPath") private val apkPath: String,
    @Assisted("appName") private val appName: String,
    @Assisted("appVersion") private val appVersion: String,
    private val exceptionProvider: ExceptionProvider,
    private val repository: ApkRepository,
    application: Application
) : AndroidViewModel(application) {

    private val apk = File(apkPath)
    val name: MutableLiveData<String> = MutableLiveData(appName)
    val lastError: MutableLiveData<LastError> = MutableLiveData(LastError.NO_ERROR)
    val analysis: MutableLiveData<AnalysisMap?> = MutableLiveData(null)
    val loading: MutableLiveData<Boolean> = MutableLiveData(false)

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("apkPath") apk: String,
            @Assisted("appName") appName: String,
            @Assisted("appVersion") appVersion: String,
        ): VirusTotalViewModel
    }

    fun getApk(): File = apk

    fun uploadFile() {
        viewModelScope.launch {
            loading.value = true
            val file = getFileByHashAsync(apk.sha256())
            if (file == null) {
                val id = repository.uploadFile(apk, "${appName}_$appVersion.apk")
                if (id != null) {
                    analysis.value = repository.getAnalysisById(id)
                }
            } else {
                analysis.value = file.lastAnalysisResults
            }
            lastError.value = exceptionProvider.getLastError()
            loading.value = false
        }
    }

    fun getFileByHash(hash: String) {
        viewModelScope.launch {
            loading.value = true
            val file = getFileByHashAsync(hash)
            analysis.value = file?.lastAnalysisResults
            lastError.value = exceptionProvider.getLastError()
            loading.value = false
        }
    }

    private suspend fun getFileByHashAsync(hash: String): VirusTotalFile? {
        return repository.getFileByHash(hash)
    }
}