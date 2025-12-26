package com.example.sipakjabat.data.model

data class RegisterRequest(
    val namaLengkap: String,
    val nip: String,
    val email: String,
    val password: String,
    val pangkatGolongan: String,
    val jabatan: String,
    val tmtPangkatTerakhir: String, // Format "YYYY-MM-DD"
    val role: String                // "PEGAWAI" atau "VERIFIKATOR"
)