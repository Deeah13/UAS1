package com.example.sipakjabat.data.model

data class VerifikasiRequest(
    val status: String, // "APPROVED" atau "REJECTED"
    val catatan: String? = null
)