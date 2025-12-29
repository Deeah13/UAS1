package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("nip") val nip: String,
    @SerializedName("namaLengkap") val namaLengkap: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("pangkatGolongan") val pangkatGolongan: String?,
    @SerializedName("jabatan") val jabatan: String?,
    @SerializedName("tmtPangkatTerakhir") val tmtPangkatTerakhir: String?,
    @SerializedName("noHp") val noHp: String?
)

