package ru.netology.app_manager.core.apk.models

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.netology.app_manager.core.apk.manager.ApkExtractor
import ru.netology.app_manager.core.apk.manager.ApkExtractor.isSystemPackage

@Parcelize
data class Apk(
    val appInfo: ApplicationInfo,
    val appName: String,
    val packageName: String,
    val version: String,
    val installDate: Long,
    val isSystem: Boolean,
    val isRunning: Boolean,
) : Parcelable {

    companion object {
        fun fromPackageInfo(info: PackageInfo, context: Context): Apk {
            val applicationInfo = info.applicationInfo
            return Apk(
                appInfo = applicationInfo,
                appName = context.packageManager.getApplicationLabel(applicationInfo).toString(),
                installDate = info.firstInstallTime,
                packageName = info.packageName,
                version = info.versionName,
                isSystem = info.isSystemPackage(),
                isRunning = ApkExtractor.isAppRunning(info.packageName)
            )
        }
    }
}