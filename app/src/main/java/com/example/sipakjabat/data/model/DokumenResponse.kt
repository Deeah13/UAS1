package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

data class DokumenResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("jenisDokumen")
    val jenisDokumen: String,
    @SerializedName("nomorDokumen")
    val nomorDokumen: String,
    @SerializedName("tanggalTerbit")
    val tanggalTerbit: String,
    @SerializedName("deskripsi")
    val deskripsi: String? = null
)

