package com.example.sipakjabat.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("AUTH_PREF", Context.MODE_PRIVATE)

    // Perbaikan: Gunakan Long untuk parameter userId
    fun saveAuthData(token: String, role: String, userId: Long) {
        prefs.edit()
            .putString("TOKEN", token)
            .putString("ROLE", role)
            .putLong("USER_ID", userId) // Menggunakan putLong untuk menyimpan Long
            .apply()
    }

    fun getToken(): String? {
        return prefs.getString("TOKEN", null)
    }

    fun getRole(): String? {
        return prefs.getString("ROLE", null)
    }

    // Perbaikan: Gunakan getLong dan kembalikan tipe Long
    fun getUserId(): Long {
        return prefs.getLong("USER_ID", -1L) // Menggunakan getLong untuk mengambil Long
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}