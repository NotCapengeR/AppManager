package ru.netology.app_manager.core.apk.models

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import android.os.Build.VERSION_CODES.*
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.netology.app_manager.core.apk.manager.ApkExtractor
import ru.netology.app_manager.utils.*
import ru.netology.app_manager.utils.HashUtils.md5
import ru.netology.app_manager.utils.HashUtils.sha1
import ru.netology.app_manager.utils.HashUtils.sha256
import java.io.File


@Parcelize
data class AppInfo(
    val appInfo: ApplicationInfo,
    val appName: String,
    val version: String,
    val packageName: String,
    val installDate: Long,
    val permissions: List<String>?,
    val features: List<String>?,
    val sourceApk: File,
    val apkSize: Long, // bytes
    val appSize: Long,
    val targetSdkVersion: Int,
    val minSdkVersion: Int?,
    val compileSdkVersion: Int?,
    val sha256: String,
    val sha1: String,
    val md5: String,
) : Parcelable {

    companion object {
        fun fromPackageInfo(info: PackageInfo, context: Context): AppInfo {
            val applicationInfo = info.applicationInfo
            val sourceApk = File(applicationInfo.sourceDir)
            val minSdkVersion =
                if (Build.VERSION.SDK_INT >= N) applicationInfo.minSdkVersion else null
            val compileSdkVersion =
                if (Build.VERSION.SDK_INT >= S) applicationInfo.compileSdkVersion else null
            val appSize =
                if (Build.VERSION.SDK_INT >= O) ApkExtractor.getAppSize26(context, applicationInfo) else 0L
            return AppInfo(
                appInfo = applicationInfo,
                packageName = info.packageName,
                appName = context.packageManager.getApplicationLabel(applicationInfo).toString(),
                apkSize = sourceApk.length(),
                permissions = info.permissions?.map { it.name }?.combineNotNull(info.requestedPermissions?.toList()),
                installDate = info.firstInstallTime,
                version = info.versionName,
                minSdkVersion = minSdkVersion,
                appSize = appSize,
                features = info.reqFeatures?.map { it.name },
                compileSdkVersion = compileSdkVersion,
                targetSdkVersion = applicationInfo.targetSdkVersion,
                sourceApk = sourceApk,
                sha1 = sourceApk.sha1(),
                sha256 = sourceApk.sha256(),
                md5 = sourceApk.md5()
            )
        }
    }

}