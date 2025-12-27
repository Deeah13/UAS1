package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

data class AdminCreateUserRequest(
    @SerializedName("namaLengkap") val namaLengkap: String,
    @SerializedName("nip") val nip: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("pangkatGolongan") val pangkatGolongan: String,
    @SerializedName("jabatan") val jabatan: String,
    // Gunakan format "yyyy-MM-dd"
    @SerializedName("tmtPangkatTerakhir") val tmtPangkatTerakhir: String,
    // Role dikirim sebagai String "PEGAWAI" atau "VERIFIKATOR"
    @SerializedName("role") val role: String
)