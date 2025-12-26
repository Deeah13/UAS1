package com.example.sipakjabat.data.api

import com.example.sipakjabat.data.model.*
import com.example.sipakjabat.data.model.PegawaiResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Pegawai: Ambil daftar dokumen master
    @GET("api/pegawai/dokumen")
    suspend fun getMyDocuments(): Response<GlobalResponse<List<DokumenResponse>>>

    // Lokasi: com.example.sipakjabat.data.api.ApiService
    @POST("api/pegawai/pengajuan")
    fun createPengajuan(
        @Header("Authorization") token: String,
        @Body request: PengajuanCreateRequestDTO
    ): Response<GlobalResponse<PengajuanResponse>>

    // Pegawai: Ambil detail pengajuan (Single Source of Truth)
    @GET("api/pegawai/pengajuan/{id}")
    suspend fun getDetailPengajuan(
        @Path("id") id: Long
    ): Response<GlobalResponse<PengajuanResponse>>

    // Pegawai: Lampirkan dokumen langsung ke backend
    @POST("api/pegawai/pengajuan/{id}/lampirkan")
    suspend fun lampirkanDokumen(
        @Path("id") id: Long,
        @Body request: LampirkanDokumenRequest
    ): Response<GlobalResponse<PengajuanResponse>>

    // Pegawai: Submit draf menjadi submitted
    @POST("api/pegawai/pengajuan/{id}/submit")
    suspend fun submitPengajuan(
        @Path("id") id: Long
    ): Response<GlobalResponse<PengajuanResponse>>

    @DELETE("api/pegawai/pengajuan/{id}")
    suspend fun deletePengajuan(
        @Path("id") id: Long
    ): Response<GlobalResponse<String>>

    // Auth & Profile (Gunakan suspend juga agar seragam)
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/pegawai/profile")
    suspend fun getProfile(): Response<GlobalResponse<PegawaiResponse>>
}