package ru.netology.app_manager.core.helper.exceptions

import android.content.Context
import ru.netology.app_manager.core.helper.network.NetworkResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionProvider @Inject constructor(
    private val context: Context
) {
    private var lastError: LastError = LastError.NO_ERROR

    fun getLastError(): LastError {
        return lastError.also {
            setLastError(LastError.NO_ERROR)
        }
    }

    fun setLastError(error: LastError = LastError.NO_ERROR) {
        lastError = error
    }

    fun setLastError(code: Int, message: String?, error: Throwable) {
        lastError = LastError(code, message, error)
    }

    fun setLastError(result: NetworkResult<*>) {
        lastError = LastError.fromNetworkResult(result)
    }
}