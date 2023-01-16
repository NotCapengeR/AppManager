package ru.netology.app_manager.core.apk.models

import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ActivityData(
    val packageName: String,
    val name: String,
    val permission: String?,
    val flags: Int,
    val appInfo: ApplicationInfo,
    val launchMode: Int
) : Parcelable {

    companion object {
        fun fromActivityInfo(info: ActivityInfo): ActivityData {
            return ActivityData(
                packageName = info.packageName,
                launchMode = info.launchMode,
                appInfo = info.applicationInfo,
                name = info.name,
                permission = info.permission,
                flags = info.flags
            )
        }

        fun fromPackageInfo(info: PackageInfo): List<ActivityData> {
            return if (info.activities.isNullOrEmpty()) {
                emptyList()
            } else {
                info.activities.map { fromActivityInfo(it) }
            }
        }
    }
}