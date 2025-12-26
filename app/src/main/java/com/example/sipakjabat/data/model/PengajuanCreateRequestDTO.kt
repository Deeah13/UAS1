package com.example.sipakjabat.data.model

data class PengajuanCreateRequestDTO(
    val jenisPengajuan: String, // REGULER, FUNGSIONAL, atau STRUKTURAL
    val pangkatTujuan: String,
    val jabatanTujuan: String
)