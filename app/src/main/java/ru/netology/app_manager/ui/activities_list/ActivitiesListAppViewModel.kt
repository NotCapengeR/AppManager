package ru.netology.app_manager.ui.activities_list

import android.app.Application
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
import ru.netology.app_manager.core.apk.models.ActivityData
import ru.netology.app_manager.core.apk.models.AppInfo

class ActivitiesListAppViewModel @AssistedInject constructor(
    application: Application,
    @Assisted info: AppInfo
) : AndroidViewModel(application) {

    val app: MutableLiveData<AppInfo> = MutableLiveData(info)
    val activities: MutableLiveData<List<ActivityData>> = MutableLiveData(emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val list = getAllActivitiesFromApp(info.packageName)
            withContext(Dispatchers.Main) {
                activities.value = list
            }
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(info: AppInfo): ActivitiesListAppViewModel
    }

    private fun getAllActivitiesFromApp(packageName: String): List<ActivityData> =
        ApkExtractor.getAllActivitiesFromApp(
            getApplication(),
            packageName
        )

}