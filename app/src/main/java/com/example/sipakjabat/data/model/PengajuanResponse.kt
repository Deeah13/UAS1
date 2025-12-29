package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

data class PengajuanResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("jenisPengajuan") val jenisPengajuan: String,
    @SerializedName("pangkatSaatIni") val pangkatSaatIni: String?,
    @SerializedName("jabatanSaatIni") val jabatanSaatIni: String?,
    @SerializedName("pangkatTujuan") val pangkatTujuan: String?,
    @SerializedName("jabatanTujuan") val jabatanTujuan: String?,
    @SerializedName("status") val status: String,
    @SerializedName("tanggalDibuat") val tanggalDibuat: String,
    // Menangkap data user pengaju (NAMA, NIP, EMAIL)
    @SerializedName("user") val user: UserSimpleResponse? = null,
    @SerializedName("lampiran") val lampiran: List<DokumenResponse> = emptyList(),
    @SerializedName("catatanVerifikator") val catatanVerifikator: String? = null,
    @SerializedName("alasanPenolakan") val alasanPenolakan: String? = null
)

data class UserSimpleResponse(
    @SerializedName("namaLengkap") val namaLengkap: String,
    @SerializedName("nip") val nip: String,
    @SerializedName("email") val email: String
)