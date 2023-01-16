package ru.netology.app_manager.core.helper.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsManager @Inject constructor(
    private val preferences: SharedPreferences
) {


    val size: Int
        get() = preferences.all.size

    operator fun contains(key: String): Boolean = preferences.contains(key)

    fun remove(key: String) {
        preferences.edit {
            remove(key)
        }
    }

    fun clear() {
        if (size == 0) return
        preferences.edit {
            clear()
        }
    }

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long =
        preferences.getLong(key, defaultValue)


    fun getInt(key: String, defaultValue: Int = 0): Int = preferences.getInt(key, defaultValue)


    fun getFloat(key: String, defaultValue: Float = 0f): Float =
        preferences.getFloat(key, defaultValue)


    fun getBoolean(key: String, defaultValue: Boolean): Boolean = preferences.getBoolean(key, defaultValue)


    fun getString(key: String, defaultValue: String? = null): String? = preferences.getString(key, defaultValue)

    fun getStringSet(key: String, defaultValue: Set<String>? = null): Set<String>? =
        preferences.getStringSet(key, defaultValue)

    fun putLong(key: String, value: Long) {
        preferences.edit {
            putLong(key, value)
        }
    }

    fun putFloat(key: String, value: Float) {
        preferences.edit {
            putFloat(key, value)
        }
    }

    fun putInt(key: String, value: Int) {
        preferences.edit {
            putInt(key, value)
        }
    }

    fun putString(key: String, value: String?) {
        preferences.edit {
            putString(key, value)
        }
    }

    fun putStringSet(key: String, value: Set<String>?) {
        preferences.edit {
            putStringSet(key, value)
        }
    }

    fun edit(commit: Boolean = false, action: SharedPreferences.Editor.() -> Unit) {
        preferences.edit(commit, action)
    }


    companion object {
        const val SHARED_PREFS_NAME: String = "App manager shared prefs"

        //Prefs key
        const val TOKEN_KEY: String = "usr_token"
    }

}