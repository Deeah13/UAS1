package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

// Sesuaikan nama class dengan yang Anda gunakan di frontend
data class SummaryResponseDTO(
    @SerializedName("jumlahSubmitted") val jumlahSubmitted: Int,
    @SerializedName("jumlahApproved") val jumlahApproved: Int,
    @SerializedName("jumlahRejected") val jumlahRejected: Int,
    // Tambahkan field ini agar error 'Unresolved reference' hilang
    @SerializedName("jumlahRevisi") val jumlahRevisi: Int
)