package com.example.sipakjabat.data.model

data class AuthResponse(
    val token: String,
    val userId: Int, // Tambahkan ini sesuai tipe data di backend (Int/Long)
    val role: String  // Tambahkan ini agar bisa disimpan ke TokenManager
)