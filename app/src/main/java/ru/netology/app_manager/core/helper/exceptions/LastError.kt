package ru.netology.app_manager.core.helper.exceptions

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.HttpException
import ru.netology.app_manager.core.api.exceptions.AlreadyExistsException
import ru.netology.app_manager.core.api.exceptions.ApiObjectNotFoundException
import ru.netology.app_manager.core.api.models.Message
import ru.netology.app_manager.core.helper.network.NetworkResult
import ru.netology.app_manager.core.helper.network.exceptions.FailedHttpRequestException
import ru.netology.app_manager.utils.getErrorMessage
import timber.log.Timber
import java.lang.Error

@Parcelize
data class LastError(
    val code: Int,
    override val message: String?,
    val error: Throwable
) : Parcelable, Throwable(message, error) {

    companion object {

        //error codes
        const val ERROR_SUCCESS = 0
        const val ERROR_ALREADY_EXISTS = 1830
        const val ERROR_CONTINUE = 1
        const val ERROR_BAD_REQUEST: Int = 400
        const val ERROR_UNAUTHORIZED: Int = 401
        const val ERROR_PAYMENT_REQUIRED: Int = 402
        const val ERROR_FORBIDDEN: Int = 403
        const val ERROR_NOT_FOUND: Int = 404
        const val ERROR_NOT_ALLOWED: Int = 405
        const val ERROR_NOT_ACCEPTABLE: Int = 406
        const val ERROR_BAD_GATEWAY: Int = 502
        const val ERROR_TIMEOUT_GATEWAY: Int = 504


        val NO_ERROR = LastError(
            ERROR_SUCCESS,
            null,
            NoError
        )

        val LOADING = LastError(
            ERROR_CONTINUE,
            null,
            NoError
        )

        fun fromNetworkResult(result: NetworkResult<*>): LastError {
            return when (result) {
                is NetworkResult.Success -> NO_ERROR
                is NetworkResult.Error -> {
                    val exception = result.error
                    if (exception is FailedHttpRequestException) {
                        val body = exception.response.errorBody()
                        try {
                            val rawMessage = body?.string()
                            if (rawMessage != null) {
                                val message = Json.decodeFromString<Message>(rawMessage)
                                Timber.d(message.message)
                                return LastError(result.code, message.message, result.error)
                            }
                        } catch (ex: Exception) {
                            Timber.e(ex.getErrorMessage())
                        }
                    }
                    return LastError(result.code, result.error.getErrorMessage(), result.error)
                }
                NetworkResult.Loading -> LOADING
            }
        }
    }

    fun errorNotFound(message: String? = null, cause: Throwable? = null): LastError {
        return LastError(
            code = ERROR_NOT_FOUND,
            message = message,
            error = ApiObjectNotFoundException(message, cause)
        )
    }

    fun errorAlreadyExists(message: String? = null, cause: Throwable? = null): LastError {
        return LastError(
            code = ERROR_ALREADY_EXISTS,
            message = message,
            error = AlreadyExistsException(message, cause)
        )
    }
}


object NoError : Error(null, null)

