package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

data class SummaryResponseDTO(
    @SerializedName("jumlahSubmitted") val jumlahSubmitted: Int,
    @SerializedName("jumlahApproved") val jumlahApproved: Int,
    @SerializedName("jumlahRejected") val jumlahRejected: Int,
    @SerializedName("jumlahRevisi") val jumlahRevisi: Int
)

