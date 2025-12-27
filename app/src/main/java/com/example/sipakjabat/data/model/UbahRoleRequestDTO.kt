package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

data class UbahRoleRequestDTO(
    // Gunakan String agar mudah diproses Retrofit ("PEGAWAI" / "VERIFIKATOR")
    @SerializedName("newRole") val newRole: String
)