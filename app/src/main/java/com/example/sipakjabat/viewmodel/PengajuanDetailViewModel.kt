package com.example.sipakjabat.viewmodel

import android.util.Log
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.DokumenResponse
import com.example.sipakjabat.data.model.LampirkanDokumenRequest
import com.example.sipakjabat.data.model.PengajuanResponse
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailPengajuanUiState(
    val isLoading: Boolean = false,
    val pengajuan: PengajuanResponse? = null,
    val availableDocuments: List<DokumenResponse> = emptyList(),
    val selectedDocumentIds: Set<Long> = emptySet(),
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val isEditable: Boolean
        get() = pengajuan?.status == "DRAFT"

    val canAttachDocuments: Boolean
        get() = pengajuan?.status == "DRAFT"

    val canSubmit: Boolean
        get() = pengajuan?.status == "DRAFT" && (pengajuan.lampiran?.isNotEmpty() ?: false)

    val statusColor: Int
        get() = when (pengajuan?.status) {
            "DRAFT" -> "#FFA500".toColorInt()
            "SUBMITTED" -> "#2196F3".toColorInt()
            "APPROVED" -> "#4CAF50".toColorInt()
            "REJECTED" -> "#F44336".toColorInt()
            "PERLU_REVISI" -> "#FF9800".toColorInt()
            else -> "#808080".toColorInt() // Gray
        }

    val statusText: String
        get() = when (pengajuan?.status) {
            "DRAFT" -> "Draft"
            "SUBMITTED" -> "Menunggu Verifikasi"
            "APPROVED" -> "Disetujui"
            "REJECTED" -> "Ditolak"
            "PERLU_REVISI" -> "Perlu Revisi"
            else -> "Unknown"
        }
}

class PengajuanDetailViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailPengajuanUiState())
    val uiState: StateFlow<DetailPengajuanUiState> = _uiState.asStateFlow()

    private val apiService = RetrofitClient.instance

    private fun getToken(): String {
        return "Bearer ${tokenManager.getToken()}"
    }

    fun loadDetailPengajuan(pengajuanId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val response = apiService.getDetailPengajuan(getToken(), pengajuanId)

                if (response.isSuccessful && response.body()?.data != null) {
                    val pengajuan = response.body()!!.data!!
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pengajuan = pengajuan,
                        selectedDocumentIds = pengajuan.lampiran?.map { it.id }?.toSet() ?: emptySet()
                    )

                    if (pengajuan.status == "DRAFT") {
                        loadAvailableDocuments()
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = response.body()?.message ?: "Gagal memuat detail pengajuan"
                    )
                }
            } catch (e: Exception) {
                Log.e("PengajuanDetailVM", "Error loading detail: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Terjadi kesalahan: ${e.message}"
                )
            }
        }
    }

    private fun loadAvailableDocuments() {
        viewModelScope.launch {
            try {
                val response = apiService.getMyDocuments(getToken())

                if (response.isSuccessful && response.body()?.data != null) {
                    _uiState.value = _uiState.value.copy(
                        availableDocuments = response.body()!!.data!!
                    )
                }
            } catch (e: Exception) {
                Log.e("PengajuanDetailVM", "Error loading documents: ${e.message}", e)
            }
        }
    }

    fun toggleDocumentSelection(documentId: Long) {
        val currentSelection = _uiState.value.selectedDocumentIds.toMutableSet()
        if (currentSelection.contains(documentId)) {
            currentSelection.remove(documentId)
        } else {
            currentSelection.add(documentId)
        }
        _uiState.value = _uiState.value.copy(selectedDocumentIds = currentSelection)
    }

    fun attachDocuments(pengajuanId: Long) {
        val selectedIds = _uiState.value.selectedDocumentIds

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val request = LampirkanDokumenRequest(selectedIds.toList())
                val response = apiService.lampirkanDokumen(getToken(), pengajuanId, request)

                if (response.isSuccessful && response.body()?.data != null) {
                    val updatedPengajuan = response.body()!!.data!!
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pengajuan = updatedPengajuan,
                        successMessage = "Dokumen berhasil dilampirkan",
                        selectedDocumentIds = updatedPengajuan.lampiran?.map { it.id }?.toSet() ?: emptySet()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = response.body()?.message ?: "Gagal melampirkan dokumen"
                    )
                }
            } catch (e: Exception) {
                Log.e("PengajuanDetailVM", "Error attaching documents: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Terjadi kesalahan: ${e.message}"
                )
            }
        }
    }

    fun submitPengajuan(pengajuanId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val response = apiService.submitPengajuan(getToken(), pengajuanId)

                if (response.isSuccessful && response.body()?.data != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pengajuan = response.body()!!.data!!,
                        successMessage = "Pengajuan berhasil disubmit"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = response.body()?.message ?: "Gagal submit pengajuan"
                    )
                }
            } catch (e: Exception) {
                Log.e("PengajuanDetailVM", "Error submitting: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Terjadi kesalahan: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}