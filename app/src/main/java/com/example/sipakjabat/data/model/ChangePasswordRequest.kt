package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

data class ChangePasswordRequest(
    @SerializedName("currentPassword") // Sesuaikan dengan Backend Java
    val currentPassword: String,
    @SerializedName("newPassword")
    val newPassword: String
)