package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

data class PegawaiResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("nama")
    val nama: String,

    @SerializedName("nip")
    val nip: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("pangkatSaatIni")
    val pangkatSaatIni: String?,

    @SerializedName("jabatanSaatIni")
    val jabatanSaatIni: String?
)