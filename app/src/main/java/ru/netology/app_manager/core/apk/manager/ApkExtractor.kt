package ru.netology.app_manager.core.apk.manager

import android.Manifest
import android.app.PendingIntent
import android.app.usage.StorageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.RecoverySystem.installPackage
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import com.github.florent37.runtimepermission.kotlin.askPermission
import org.apache.commons.io.FileUtils
import ru.netology.app_manager.core.apk.models.ActivityData
import ru.netology.app_manager.core.apk.models.Apk
import ru.netology.app_manager.core.apk.models.AppInfo
import ru.netology.app_manager.utils.AndroidUtils.showToast
import ru.netology.app_manager.utils.getErrorMessage
import timber.log.Timber
import java.io.*


object ApkExtractor {

    const val ACTION_INSTALL_COMPLETE = "cm.android.intent.action.INSTALL_COMPLETE"

    private fun checkExternalStorage(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun getAppFolder(): File? {
        if (checkExternalStorage()) {
            return File(Environment.getExternalStorageDirectory(), "ExtractedApks")
        }
        return null
    }

    private fun makeAppDir() {
        val file = getAppFolder()
        if (file != null && !file.exists()) {
            file.mkdir()
        }
    }

    private fun extractApkFile(apk: Apk): Boolean {
        makeAppDir()
        val originalFile = File(apk.appInfo.sourceDir)
        val extractedFile: File = getApkFile(apk) ?: return false
        return try {
            FileUtils.copyFile(originalFile, extractedFile)
            true
        } catch (t: Throwable) {
            false
        }
    }

    private fun extractApkFile(info: AppInfo): Boolean {
        makeAppDir()
        val originalFile = info.sourceApk
        val extractedFile: File = getApkFile(info) ?: return false
        return try {
            FileUtils.copyFile(originalFile, extractedFile)
            true
        } catch (t: Throwable) {
            false
        }
    }

    private fun getApkFile(appName: String, appVersion: String): File? {
        val folder = getAppFolder() ?: return null
        return File("${folder.path}${File.separator}${appName}_${appVersion}.apk")
    }

    fun getApkFile(info: AppInfo): File? {
        return getApkFile(info.appName, info.version)
    }

    fun getApkFile(apk: Apk): File? {
        return getApkFile(apk.appName, apk.version)
    }

    fun getApp(context: Context, packageName: String, flag: Int): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(flag.toLong())
            )
        } else {
            context.packageManager.getPackageInfo(packageName, flag)
        }
    }

    fun getAllApps(context: Context, flag: Int): List<PackageInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(flag.toLong()))
        } else {
            context.packageManager.getInstalledPackages(flag)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAppSize26(context: Context, info: ApplicationInfo): Long {
        val storageStatsManager =
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        return try {
            val storageStats = storageStatsManager.queryStatsForUid(info.storageUuid, info.uid)
            storageStats.dataBytes
        } catch (t: Throwable) {
            Timber.e("(${t::class.simpleName}) Error getting app size: ${t.getErrorMessage()}}")
            0L
        }
    }

    fun uninstallAppRoot(packageName: String, context: Context) {
        val intent = Intent(context, context::class.java)
        val sender = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val installer = context.packageManager.packageInstaller
        installer.uninstall(packageName, sender.intentSender)
    }

    @Throws(IOException::class)
    fun installPackage(context: Context, `in`: InputStream, packageName: String): Boolean {
        return try {
            val packageInstaller = context.packageManager.packageInstaller
            val params = SessionParams(
                SessionParams.MODE_FULL_INSTALL
            )
            params.setAppPackageName(packageName)
            // set params
            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)
            val `out`: OutputStream = session.openWrite("COSU", 0, -1)
            val buffer = ByteArray(65536)
            var len: Int
            while (`in`.read(buffer).also { len = it } != -1) {
                `out`.write(buffer, 0, len)
            }
            session.fsync(`out`)
            `in`.close()
            `out`.close()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                Intent(ACTION_INSTALL_COMPLETE),
                PendingIntent.FLAG_IMMUTABLE
            )
            session.commit(pendingIntent.intentSender)
            true
        } catch (t: Throwable) {
            Timber.e(" (${t::class.simpleName}) Error occurred while installing application: ${t.getErrorMessage()}")
            false
        }
    }

    fun installPackage(context: Context, file: File, packageName: String) {
        installPackage(context, FileInputStream(file), packageName)
    }

    fun isAppRunning(packageName: String): Boolean {
        var isRunning = false
        try {
            val process = Runtime.getRuntime().exec("ps")
            val `in` = BufferedReader(InputStreamReader(process.inputStream))
            var read: Int
            val buffer = CharArray(4096)
            val output = StringBuffer()
            while (`in`.read(buffer).also { read = it } > 0) {
                output.append(buffer, 0, read)
                if (output.toString().contains(packageName)) {
                    isRunning = true
                }
            }
            `in`.close()
            if (output.toString().contains(packageName)) {
                isRunning = true
            }
        } catch (e: IOException) {
            Timber.e("Running processes shell command failed execution: ${e.getErrorMessage()}")
        }
        return isRunning
    }

    fun PackageInfo.isSystemPackage(): Boolean {
        return (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }

    inline fun Fragment.withExternalStoragePermission(crossinline action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            askPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE) {
                action.invoke()
            }
        } else {
            askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                action.invoke()
            }
        }
    }

    fun Fragment.extractApkWithPermissions(
        info: AppInfo,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        withExternalStoragePermission {
            extractApk(info, onSuccess, onError)
        }
    }

    fun extractApk(
        info: AppInfo,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ): Boolean {
        if (extractApkFile(info)) {
            val file = getApkFile(info)
            return if (file?.exists() == true) {
                onSuccess.invoke()
                true
            } else {
                onError.invoke()
                false
            }
        }
        return false
    }

    fun extractApk(
        apk: Apk,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ): Boolean {
        if (extractApkFile(apk)) {
            val file = getApkFile(apk)
            return if (file?.exists() == true) {
                onSuccess.invoke()
                true
            } else {
                onError.invoke()
                false
            }
        }
        return false
    }

    fun getAllActivitiesFromApp(context: Context, packageName: String): List<ActivityData> {
        val app = getApp(context, packageName, PackageManager.GET_ACTIVITIES)
        return ActivityData.fromPackageInfo(app)
    }

    fun launchApp(packageName: String, context: Context) {
        val intent = context.packageManager?.getLaunchIntentForPackage(packageName)
        intent?.apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun launchActivity(packageName: String, activityName: String, context: Context) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName(packageName, activityName)
            }
            Timber.d("Intent for $activityName: $intent")
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        } catch (ex: SecurityException) {
            Timber.d("Couldn't open $activityName: ${ex.getErrorMessage()}")
        }

    }

    fun uninstallApp(packageName: String, context: Context) {
        Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
            context.startActivity(this)
        }
    }
}