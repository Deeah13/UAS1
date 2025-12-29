package com.example.sipakjabat.data.model

data class AuthResponse(
    val token: String,
    val userId: Int,
    val role: String
)

