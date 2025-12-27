package com.example.sipakjabat.data.api

import com.example.sipakjabat.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- 1. AUTH & PROFILE (Login & Data Pegawai) ---

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/pegawai/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<GlobalResponse<ProfileResponse>>

    @PUT("api/user/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<GlobalResponse<ProfileResponse>>

    @PUT("api/user/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<GlobalResponse<String>>


    // --- 2. MASTER DOKUMEN PEGAWAI (Tempat Simpan File SK/SKP) ---

    @GET("api/pegawai/dokumen")
    suspend fun getMyDocuments(
        @Header("Authorization") token: String
    ): Response<GlobalResponse<List<DokumenResponse>>>

    @POST("api/pegawai/dokumen")
    suspend fun createDocument(
        @Header("Authorization") token: String,
        @Body request: CreateDokumenRequest
    ): Response<GlobalResponse<DokumenResponse>>

    @DELETE("api/pegawai/dokumen/{id}")
    suspend fun deleteDocument(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<GlobalResponse<String>>


    // --- 3. PENGELOLAAN PENGAJUAN (Kenaikan Pangkat/Jabatan) ---

    @GET("api/pegawai/pengajuan")
    suspend fun getRiwayatPengajuan(
        @Header("Authorization") token: String
    ): Response<GlobalResponse<List<PengajuanResponse>>>

    @POST("api/pegawai/pengajuan")
    suspend fun createPengajuan(
        @Header("Authorization") token: String,
        @Body request: PengajuanCreateRequestDTO
    ): Response<GlobalResponse<PengajuanResponse>>

    @GET("api/pegawai/pengajuan/{id}")
    suspend fun getDetailPengajuan(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<GlobalResponse<PengajuanResponse>>

    @POST("api/pegawai/pengajuan/{id}/lampirkan")
    suspend fun lampirkanDokumen(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: LampirkanDokumenRequest
    ): Response<GlobalResponse<PengajuanResponse>>

    @POST("api/pegawai/pengajuan/{id}/submit")
    suspend fun submitPengajuan(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<GlobalResponse<PengajuanResponse>>

    @DELETE("api/pegawai/pengajuan/{id}")
    suspend fun deletePengajuan(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<GlobalResponse<String>>

    // --- 4. ENDPOINT KHUSUS ADMIN / VERIFIKATOR ---
    // Disesuaikan dengan AdminController.java di backend

    @GET("api/admin/pengajuan/all")
    suspend fun getAllPengajuan(
        @Header("Authorization") token: String
    ): Response<GlobalResponse<List<PengajuanResponse>>>

    @GET("api/admin/pengajuan/submitted")
    suspend fun getSubmittedPengajuan(
        @Header("Authorization") token: String
    ): Response<GlobalResponse<List<PengajuanResponse>>>

    @GET("api/admin/pengajuan/summary")
    suspend fun getAdminSummary(
        @Header("Authorization") token: String
    ): Response<GlobalResponse<SummaryResponseDTO>>

    @POST("api/admin/pengajuan/{id}/approve")
    suspend fun approvePengajuan(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: VerifikasiRequest
    ): Response<GlobalResponse<PengajuanResponse>>

    @POST("api/admin/pengajuan/{id}/reject")
    suspend fun rejectPengajuan(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: VerifikasiRequest
    ): Response<GlobalResponse<PengajuanResponse>>

    @POST("api/admin/pengajuan/{id}/revisi")
    suspend fun mintaRevisi(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: RevisiRequest
    ): Response<GlobalResponse<PengajuanResponse>>
}