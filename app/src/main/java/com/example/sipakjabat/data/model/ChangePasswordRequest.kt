package com.example.sipakjabat.data.model

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)