package com.example.sipakjabat.data.model

data class GlobalResponse<T>(
    val status: String,   // Biasanya berisi "success" atau "error"
    val message: String?, // Pesan dari server
    val data: T?          // Data utama (bisa null jika error)
)