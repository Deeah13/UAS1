package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class untuk request body saat membuat dokumen baru.
 * Nama variabel disesuaikan dengan field yang diterima oleh Backend Spring Boot.
 */
data class CreateDokumenRequest(
    @SerializedName("jenisDokumen")
    val jenisDokumen: String, // DB: jenis_dokumen

    @SerializedName("nomorDokumen")
    val nomorDokumen: String, // DB: nomor_dokumen

    @SerializedName("tanggalTerbit")
    val tanggalTerbit: String, // DB: tanggal_terbit (format: YYYY-MM-DD)

    @SerializedName("deskripsi")
    val deskripsi: String // DB: deskripsi
)