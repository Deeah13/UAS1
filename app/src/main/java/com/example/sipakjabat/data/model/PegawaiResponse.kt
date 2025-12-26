package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

data class PegawaiResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("namaLengkap") val namaLengkap: String,
    @SerializedName("nip") val nip: String,
    @SerializedName("email") val email: String,
    @SerializedName("pangkatSaatIni") val pangkatSaatIni: String?,
    @SerializedName("jabatanSaatIni") val jabatanSaatIni: String?,
    @SerializedName("role") val role: String
)