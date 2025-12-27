package com.example.sipakjabat.data.model

data class AdminSummaryResponse(
    val totalPengajuan: Long,
    val jumlahDraft: Long,
    val jumlahSubmitted: Long,
    val jumlahPerluRevisi: Long,
    val jumlahApproved: Long,
    val jumlahRejected: Long
)