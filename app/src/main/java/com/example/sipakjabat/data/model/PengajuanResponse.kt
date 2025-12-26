package com.example.sipakjabat.data.model

import com.google.gson.annotations.SerializedName

data class PengajuanResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("user")
    val user: UserSimple? = null,

    @SerializedName("verifikator")
    val verifikator: UserSimple? = null,

    @SerializedName("jenisPengajuan")
    val jenisPengajuan: String,

    @SerializedName("pangkatSaatIni")
    val pangkatSaatIni: String,

    @SerializedName("jabatanSaatIni")
    val jabatanSaatIni: String,

    @SerializedName("pangkatTujuan")
    val pangkatTujuan: String?,

    @SerializedName("jabatanTujuan")
    val jabatanTujuan: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("alasanPenolakan")
    val alasanPenolakan: String? = null,

    @SerializedName("catatanVerifikator")
    val catatanVerifikator: String? = null,

    @SerializedName("tanggalDibuat")
    val tanggalDibuat: String,

    @SerializedName("tanggalDiajukan")
    val tanggalDiajukan: String? = null,

    @SerializedName("tanggalDiputuskan")
    val tanggalDiputuskan: String? = null,

    // ===== TAMBAHKAN INI JIKA BELUM ADA =====
    @SerializedName("lampiran")
    val lampiran: List<DokumenResponse> = emptyList()
)

data class UserSimple(
    @SerializedName("id")
    val id: Long,

    @SerializedName("namaLengkap")
    val namaLengkap: String,

    @SerializedName("nip")
    val nip: String? = null
)