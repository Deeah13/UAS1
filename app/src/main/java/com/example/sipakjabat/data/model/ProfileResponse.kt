package com.example.sipakjabat.data.model

data class ProfileResponse(
    val id: Long,
    val namaLengkap: String,
    val nip: String,
    val email: String,
    val pangkatGolongan: String?,
    val jabatan: String?,
    val role: String
)

