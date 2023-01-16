package ru.netology.app_manager.ui.backup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.launch
import ru.netology.app_manager.core.api.repository.BackendRepository
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
    private val repository: BackendRepository
) : ViewModel() {


    val authData: MutableLiveData<LoginData> = MutableLiveData(LoginData.EMPTY)
    val isSuccess: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)


    fun setPassword(password: String) {
        authData.value = authData.value?.copy(password = password)
    }

    fun setName(username: String) {
        authData.value = authData.value?.copy(username = username)
    }

    fun setConfirmPassword(password: String) {
        authData.value = authData.value?.copy(confirmPassword = password)
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            repository.login(username, password)
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            repository.register(username, password)
        }
    }
}