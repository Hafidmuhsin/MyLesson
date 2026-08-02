package com.example.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

object AppSecurityManager {
    private const val PREF_NAME = "teacher_plan_security_prefs"
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_PIN_ENABLED = "pin_enabled"
    private const val KEY_FLAG_SECURE = "flag_secure_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isPinEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PIN_ENABLED, false)
    }

    fun setPin(context: Context, pin: String) {
        val hash = hashPin(pin)
        getPrefs(context).edit()
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
    }

    fun disablePin(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .apply()
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val savedHash = getPrefs(context).getString(KEY_PIN_HASH, null) ?: return false
        return savedHash == hashPin(pin)
    }

    fun isFlagSecureEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_FLAG_SECURE, true)
    }

    fun setFlagSecureEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_FLAG_SECURE, enabled).apply()
    }

    private fun hashPin(pin: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(("SALT_TEACHER_APP_SECURITY_2026_" + pin).toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            pin
        }
    }

    /**
     * Sanitizes input strings against potential injection, script, or null byte attacks.
     */
    fun sanitizeInput(input: String, maxLength: Int = 1000): String {
        if (input.isEmpty()) return ""
        val cleaned = input
            .replace("\u0000", "")
            .replace("<script>", "", ignoreCase = true)
            .replace("</script>", "", ignoreCase = true)
            .replace("javascript:", "", ignoreCase = true)
            .trim()
        return if (cleaned.length > maxLength) cleaned.substring(0, maxLength) else cleaned
    }
}
