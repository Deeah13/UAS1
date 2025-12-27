package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

data class SummaryResponseDTO(
    @SerializedName("totalPengajuan") val totalPengajuan: Long,
    @SerializedName("jumlahDraft") val jumlahDraft: Long,
    @SerializedName("jumlahSubmitted") val jumlahSubmitted: Long,
    @SerializedName("jumlahPerluRevisi") val jumlahPerluRevisi: Long,
    @SerializedName("jumlahApproved") val jumlahApproved: Long,
    @SerializedName("jumlahRejected") val jumlahRejected: Long
)