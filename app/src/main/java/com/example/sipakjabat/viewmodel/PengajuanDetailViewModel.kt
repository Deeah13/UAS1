package com.example.sipakjabat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sipakjabat.R
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.*
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailPengajuanUiState(
    val isLoading: Boolean = false,
    val pengajuan: PengajuanResponse? = null,
    val availableDocuments: List<DokumenResponse> = emptyList(),
    val selectedDocumentIds: Set<Long> = emptySet(),
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val isEditable: Boolean get() = pengajuan?.status == "DRAFT"
    val canAttachDocuments: Boolean get() = pengajuan?.status == "DRAFT"
    val canSubmit: Boolean get() = pengajuan?.status == "DRAFT" && (pengajuan.lampiran.isNotEmpty())

    val canDelete: Boolean get() = pengajuan?.status == "DRAFT"

    val statusText: String get() = when (pengajuan?.status) {
        "DRAFT" -> "Draft"
        "SUBMITTED" -> "Menunggu Verifikasi"
        "APPROVED" -> "Disetujui"
        "REJECTED" -> "Ditolak"
        "PERLU_REVISI" -> "Perlu Revisi"
        else -> "Proses"
    }

    val statusColorRes: Int get() = when (pengajuan?.status) {
        "DRAFT" -> R.color.status_draft_bg
        "SUBMITTED" -> R.color.status_submitted_bg
        "APPROVED" -> R.color.status_approved_bg
        "REJECTED" -> R.color.status_rejected_bg
        "PERLU_REVISI" -> R.color.status_revisi_bg
        else -> R.color.status_unknown_bg
    }
}

class PengajuanDetailViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailPengajuanUiState())
    val uiState: StateFlow<DetailPengajuanUiState> = _uiState.asStateFlow()

    private fun getAuthToken(): String = "Bearer ${tokenManager.getToken()}"

    fun loadDetailPengajuan(pengajuanId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = RetrofitClient.instance.getDetailPengajuan(getAuthToken(), pengajuanId)
                if (response.isSuccessful && response.body()?.data != null) {
                    val data = response.body()!!.data!!
                    _uiState.update { it.copy(
                        isLoading = false,
                        pengajuan = data,
                        selectedDocumentIds = data.lampiran.map { it.id }.toSet()
                    ) }
                    if (data.status == "DRAFT") loadAvailableDocuments()
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat data") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun loadAvailableDocuments() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getMyDocuments(getAuthToken())
                if (response.isSuccessful) {
                    _uiState.update { it.copy(availableDocuments = response.body()?.data ?: emptyList()) }
                }
            } catch (e: Exception) { Log.e("VM", "Load docs error: ${e.message}") }
        }
    }

    fun toggleDocumentSelection(id: Long) {
        val current = _uiState.value.selectedDocumentIds.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _uiState.update { it.copy(selectedDocumentIds = current) }
    }

    fun attachDocuments(pengajuanId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val request = LampirkanDokumenRequest(dokumenIds = _uiState.value.selectedDocumentIds.toList())
                val response = RetrofitClient.instance.lampirkanDokumen(getAuthToken(), pengajuanId, request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Lampiran diperbarui") }
                    loadDetailPengajuan(pengajuanId)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun submitPengajuan(pengajuanId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = RetrofitClient.instance.submitPengajuan(getAuthToken(), pengajuanId)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Pengajuan dikirim") }
                    loadDetailPengajuan(pengajuanId)
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal: Syarat Dokumen (SK Pangkat/SKP) belum lengkap") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun deletePengajuan(pengajuanId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Mengonsumsi API DELETE /api/pegawai/pengajuan/{id}
                val response = RetrofitClient.instance.deletePengajuan(getAuthToken(), pengajuanId)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        successMessage = "Draf pengajuan berhasil dihapus"
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Gagal menghapus draf pengajuan"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}