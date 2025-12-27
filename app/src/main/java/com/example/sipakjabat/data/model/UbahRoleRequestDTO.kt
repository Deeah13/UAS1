package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

data class UbahRoleRequestDTO(
    // Menggunakan @SerializedName agar sinkron dengan backend
    @SerializedName("newRole") val newRole: String
)