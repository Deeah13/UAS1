package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class untuk request body saat membuat dokumen baru.
 * Variabel deskripsi diubah menjadi nullable (String?) agar sesuai dengan input opsional.
 */
data class CreateDokumenRequest(
    @SerializedName("jenisDokumen")
    val jenisDokumen: String,

    @SerializedName("nomorDokumen")
    val nomorDokumen: String,

    @SerializedName("tanggalTerbit")
    val tanggalTerbit: String,

    @SerializedName("deskripsi")
    val deskripsi: String? // PERBAIKAN: Gunakan String? (nullable)
)