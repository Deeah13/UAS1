package com.example.sipakjabat.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("AUTH_PREF", Context.MODE_PRIVATE)

    fun saveAuthData(token: String, role: String) {
        prefs.edit()
            .putString("TOKEN", token)
            .putString("ROLE", role)
            .apply()
    }

    fun getToken(): String? {
        return prefs.getString("TOKEN", null)
    }

    fun getRole(): String? {
        return prefs.getString("ROLE", null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
