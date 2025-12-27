package com.example.sipakjabat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sipakjabat.databinding.ActivityDetailPengajuanBinding
import com.example.sipakjabat.databinding.DialogSelectDocumentsBinding
import com.example.sipakjabat.utils.TokenManager
import com.example.sipakjabat.viewmodel.DetailPengajuanUiState
import com.example.sipakjabat.viewmodel.PengajuanDetailViewModel
import com.example.sipakjabat.viewmodel.PengajuanViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class DetailPengajuanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPengajuanBinding
    private lateinit var viewModel: PengajuanDetailViewModel
    private lateinit var tokenManager: TokenManager
    private lateinit var lampiranAdapter: DokumenLampiranAdapter

    private var pengajuanId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPengajuanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        pengajuanId = intent.getLongExtra("PENGAJUAN_ID", -1)

        if (pengajuanId == -1L) {
            Toast.makeText(this, "ID Pengajuan tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViewModel()
        setupUI()
        setupRecyclerView()
        observeViewModel()

        viewModel.loadDetailPengajuan(pengajuanId)
    }

    private fun setupViewModel() {
        val factory = PengajuanViewModelFactory(tokenManager)
        viewModel = ViewModelProvider(this, factory)[PengajuanDetailViewModel::class.java]
    }

    private fun setupUI() {
        // PERBAIKAN UTAMA: Menghapus setSupportActionBar(binding.toolbar)
        // karena ID 'toolbar' sudah tidak ada di XML (diganti headerView)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnKelolaDokumen.setOnClickListener { showDocumentSelectionDialog() }
        binding.btnSubmit.setOnClickListener { showSubmitConfirmationDialog() }
        binding.btnDelete.setOnClickListener { showDeleteConfirmationDialog() }
    }

    private fun setupRecyclerView() {
        lampiranAdapter = DokumenLampiranAdapter()
        binding.rvDokumenLampiran.layoutManager = LinearLayoutManager(this)
        binding.rvDokumenLampiran.adapter = lampiranAdapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: DetailPengajuanUiState) {
        // Reference ke contentLayout (ScrollView utama)
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.contentLayout.visibility = if (state.isLoading) View.GONE else View.VISIBLE

        state.errorMessage?.let {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }

        state.successMessage?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            if (it.contains("dihapus", true)) finish()
            viewModel.clearMessages()
        }

        state.pengajuan?.let { pengajuan ->
            binding.tvStatus.text = state.statusText
            binding.cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, state.statusColorRes))

            binding.tvNamaDetail.text = "Nama: ${pengajuan.user?.namaLengkap ?: "-"}"
            binding.tvNipDetail.text = "NIP: ${pengajuan.user?.nip ?: "-"}"
            binding.tvEmailDetail.text = "Email: ${pengajuan.user?.email ?: "-"}"

            binding.tvJenisDetail.text = pengajuan.jenisPengajuan
            binding.tvPangkatSaatIni.text = pengajuan.pangkatSaatIni ?: "-"
            binding.tvJabatanSaatIni.text = pengajuan.jabatanSaatIni ?: "-"
            binding.tvPangkatTujuan.text = pengajuan.pangkatTujuan ?: "-"
            binding.tvJabatanTujuan.text = pengajuan.jabatanTujuan ?: "-"

            lampiranAdapter.submitList(pengajuan.lampiran)
            binding.tvLampiranEmpty.visibility = if (pengajuan.lampiran.isEmpty()) View.VISIBLE else View.GONE

            // Reference ke cardSyarat dan tvSyaratDokumen
            binding.tvSyaratDokumen.text = when (pengajuan.jenisPengajuan) {
                "REGULER" -> "Wajib melampirkan: SK Pangkat Terakhir dan SKP."
                "FUNGSIONAL" -> "Wajib melampirkan: SK Pangkat, SK Jabatan, SKP, dan PAK."
                else -> "Harap lampirkan dokumen pendukung sesuai aturan."
            }
            binding.cardSyarat.visibility = if (state.canAttachDocuments) View.VISIBLE else View.GONE

            binding.cardCatatan.visibility = if (!pengajuan.catatanVerifikator.isNullOrBlank()) View.VISIBLE else View.GONE
            binding.tvCatatanVerifikator.text = pengajuan.catatanVerifikator

            // Reference ke cardPenolakan dan tvAlasanPenolakan
            binding.cardPenolakan.visibility = if (!pengajuan.alasanPenolakan.isNullOrBlank()) View.VISIBLE else View.GONE
            binding.tvAlasanPenolakan.text = pengajuan.alasanPenolakan

            binding.btnKelolaDokumen.visibility = if (state.canAttachDocuments) View.VISIBLE else View.GONE
            binding.btnSubmit.visibility = if (state.canSubmit) View.VISIBLE else View.GONE
            binding.btnDelete.visibility = if (state.canDelete) View.VISIBLE else View.GONE
        }
    }

    private fun showDocumentSelectionDialog() {
        val state = viewModel.uiState.value
        val documents = state.availableDocuments
        if (documents.isEmpty()) {
            Toast.makeText(this, "Belum ada dokumen di Master.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogBinding = DialogSelectDocumentsBinding.inflate(layoutInflater)
        val adapter = SelectableDokumenAdapter(documents, state.selectedDocumentIds) { id ->
            viewModel.toggleDocumentSelection(id)
        }
        dialogBinding.rvDocuments.layoutManager = LinearLayoutManager(this)
        dialogBinding.rvDocuments.adapter = adapter

        MaterialAlertDialogBuilder(this)
            .setTitle("Kelola Lampiran")
            .setView(dialogBinding.root)
            .setPositiveButton("Simpan") { _, _ -> viewModel.attachDocuments(pengajuanId) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showSubmitConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Submit Pengajuan")
            .setMessage("Data tidak bisa diubah setelah dikirim. Kirim sekarang?")
            .setPositiveButton("Ya, Kirim") { _, _ -> viewModel.submitPengajuan(pengajuanId) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Draf")
            .setMessage("Apakah Anda yakin ingin menghapus draf ini?")
            .setPositiveButton("Ya, Hapus") { _, _ -> viewModel.deletePengajuan(pengajuanId) }
            .setNegativeButton("Batal", null)
            .show()
    }
}