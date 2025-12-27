package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

data class CreateDokumenRequest(
    // Buat userId menjadi nullable agar tidak wajib diisi saat role Pegawai
    @SerializedName("userId") val userId: Long? = null,
    @SerializedName("jenisDokumen") val jenisDokumen: String,
    @SerializedName("nomorDokumen") val nomorDokumen: String,
    @SerializedName("tanggalTerbit") val tanggalTerbit: String,
    @SerializedName("deskripsi") val deskripsi: String
)