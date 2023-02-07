package ru.netology.app_manager.utils

import okhttp3.ResponseBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object NetworkUtils {

    fun ResponseBody.saveAsFile(path: String): File? {
        var input: InputStream? = null
        try {
            input = this.byteStream()
            val fos = FileOutputStream(path)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            return File(path)
        } catch (e: Throwable){
            Timber.e(e.getErrorMessage())
        }
        finally {
            input?.close()
        }
        return null
    }
}