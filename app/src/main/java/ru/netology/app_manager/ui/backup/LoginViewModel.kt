package ru.netology.app_manager.ui.backup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import ru.netology.app_manager.core.api.repository.BackendRepository
import ru.netology.app_manager.core.helper.exceptions.ExceptionProvider
import timber.log.Timber
import javax.inject.Inject


data class LoginData(
    val username: String,
    val password: String,
    val confirmPassword: String
) {

    companion object {
        val EMPTY = LoginData("", "", "")
    }
}

class LoginViewModel @Inject constructor(
    private val repository: BackendRepository,
    private val exceptionProvider: ExceptionProvider
) : ViewModel() {


    val authData: MutableLiveData<LoginData> = MutableLiveData(LoginData.EMPTY)
    val isSuccess: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val errorMessage: MutableLiveData<String?> = MutableLiveData(null)


    fun setPassword(password: String) {
        authData.value = authData.value?.copy(password = password)
    }

    fun setName(username: String) {
        authData.value = authData.value?.copy(username = username)
    }

    fun setConfirmPassword(password: String) {
        authData.value = authData.value?.copy(confirmPassword = password)
    }

    fun login() {
        viewModelScope.launch {
            authData.value?.apply {
                val ret = repository.login(username, password)
                errorMessage.value = exceptionProvider.getLastError().message
                isSuccess.value = ret != null
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            authData.value?.apply {
                if (password == confirmPassword) {
                    val ret = repository.register(username, password)
                    isSuccess.value = ret != null
                } else {
                    errorMessage.value = "Error: Password mismatch!"
                }
                errorMessage.value = exceptionProvider.getLastError().message
            }
        }
    }
}