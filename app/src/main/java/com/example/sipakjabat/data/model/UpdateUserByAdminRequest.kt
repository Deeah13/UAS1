package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

data class UpdateUserByAdminRequest(
    @SerializedName("namaLengkap") val namaLengkap: String,
    @SerializedName("email") val email: String,
    @SerializedName("pangkatGolongan") val pangkatGolongan: String,
    @SerializedName("jabatan") val jabatan: String,
    // Gunakan format "yyyy-MM-dd" sesuai @JsonFormat di backend
    @SerializedName("tmtPangkatTerakhir") val tmtPangkatTerakhir: String
)