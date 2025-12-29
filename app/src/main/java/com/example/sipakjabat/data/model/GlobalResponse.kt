package com.example.sipakjabat.data.model

data class GlobalResponse<T>(
    val status: String,
    val message: String?,
    val data: T?
)

