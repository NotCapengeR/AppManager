package ru.netology.app_manager.ui.app_list

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.netology.app_manager.core.apk.manager.ApkExtractor
import ru.netology.app_manager.core.apk.manager.ApkExtractor.getApp
import ru.netology.app_manager.core.apk.manager.ApkRepository
import ru.netology.app_manager.core.apk.models.AppInfo
import timber.log.Timber

class AppDetailsViewModel @AssistedInject constructor(
    private val repository: ApkRepository,
    @Assisted private val packageName: String,
    application: Application
) : AndroidViewModel(application) {

    val app: MutableLiveData<AppInfo?> = MutableLiveData(null)
    val appInfoVisible: MutableLiveData<Boolean> = MutableLiveData(true)
    val appPermissionsVisible: MutableLiveData<Boolean> = MutableLiveData(false)
    val checkSumVisible: MutableLiveData<Boolean> = MutableLiveData(true)
    val appFeaturesVisible: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val appInfo = AppInfo.fromPackageInfo(getApp(), getApplication())
            withContext(Dispatchers.Main) {
                app.value = appInfo
            }
        }
    }

    fun changeAppInfoVisible() {
        appInfoVisible.value = appInfoVisible.value?.not()
    }

    fun changePermissionsVisible() {
        appPermissionsVisible.value = appPermissionsVisible.value?.not()
    }


    fun changeSumVisible() {
        checkSumVisible.value = checkSumVisible.value?.not()
    }

    fun changeFeaturesVisible() {
        appFeaturesVisible.value = appFeaturesVisible.value?.not()
    }

    @AssistedFactory
    interface Factory {
        fun create(packageName: String): AppDetailsViewModel
    }


    private fun getApp(): PackageInfo {
        return ApkExtractor.getApp(
            getApplication(),
            packageName,
            PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA
        )
    }
}