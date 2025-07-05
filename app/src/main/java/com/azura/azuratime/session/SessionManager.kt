package com.azura.azuratime.session

import android.content.Context
import android.content.SharedPreferences
import com.azura.azuratime.db.UserEntity

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("azura_session", Context.MODE_PRIVATE)

    fun saveUserSession(user: UserEntity) {
        prefs.edit()
            .putString("username", user.username)
            .putString("role", user.role)
            .putString("name", user.name)
            .apply()
    }

    fun getUsername(): String? = prefs.getString("username", null)
    fun getRole(): String? = prefs.getString("role", null)
    fun getName(): String? = prefs.getString("name", null)

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getUsername() != null
}
