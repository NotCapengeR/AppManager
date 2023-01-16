package ru.netology.app_manager.core.api.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.app_manager.core.helper.prefs.PrefsManager
import ru.netology.app_manager.core.helper.prefs.PrefsManager.Companion.TOKEN_KEY
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val prefs: PrefsManager
) {

    private val _token = MutableStateFlow<String?>(null)
    val token = _token.asStateFlow()

    init {
        val jwtToken = prefs.getString(TOKEN_KEY)
        if (jwtToken != null) {
            _token.value = jwtToken
        }
    }

    @Synchronized
    fun logout() {
        _token.value = null.also {
            prefs.putString(TOKEN_KEY, null)
        }
    }

    @Synchronized
    fun setToken(token: String?) {
        _token.value = token.also {
            prefs.putString(TOKEN_KEY, it)
        }
    }
}