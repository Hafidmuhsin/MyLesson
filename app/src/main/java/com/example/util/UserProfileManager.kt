package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserProfileManager {
    private const val PREF_NAME = "user_profile_prefs"
    private const val KEY_USERNAME = "username"

    private val _username = MutableStateFlow("faculty")
    val username: StateFlow<String> = _username.asStateFlow()

    fun init(context: Context) {
        val prefs = getPrefs(context)
        val saved = prefs.getString(KEY_USERNAME, "faculty") ?: "faculty"
        _username.value = saved.removePrefix("@").trim().ifEmpty { "faculty" }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun updateUsername(context: Context, newUsername: String) {
        val cleanHandle = newUsername.trim().removePrefix("@").filter { it.isLetterOrDigit() || it == '_' || it == '.' }.ifEmpty { "faculty" }
        getPrefs(context).edit().putString(KEY_USERNAME, cleanHandle).apply()
        _username.value = cleanHandle
    }
}
