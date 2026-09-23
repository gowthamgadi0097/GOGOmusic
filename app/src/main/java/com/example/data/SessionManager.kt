package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sound_vault_auth", Context.MODE_PRIVATE)

    private val _currentUserId = MutableStateFlow<String?>(prefs.getString(KEY_USER_ID, null))
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    fun isLoggedIn(): Boolean = _currentUserId.value != null

    fun setLoggedInUser(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
        _currentUserId.value = userId
    }

    fun logout() {
        prefs.edit().remove(KEY_USER_ID).apply()
        _currentUserId.value = null
    }

    companion object {
        private const val KEY_USER_ID = "logged_in_user_id"

        fun hashPassword(password: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(("sound_vault_salt_" + password).toByteArray())
            return hashBytes.joinToString("") { "%02x".format(it) }
        }
    }
}
