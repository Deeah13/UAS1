package com.example.sipakjabat

import android.content.DialogInterface
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
        // Upgrade Toolbar & Navigation
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Sembunyikan judul bawaan sipakjabat

        // Klik tombol back manual sesuai layout baru
        binding.btnBack.setOnClickListener { finish() }

        // Tombol Aksi
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
        // Handle Loading State
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.contentLayout.visibility = if (state.isLoading) View.GONE else View.VISIBLE

        // Menampilkan Pesan Error/Sukses
        state.errorMessage?.let {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }

        state.successMessage?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            if (it == "Draf pengajuan berhasil dihapus") {
                finish()
            }
            viewModel.clearMessages()
        }

        state.pengajuan?.let { pengajuan ->
            // 1. Badge Status
            binding.tvStatus.text = state.statusText
            val color = ContextCompat.getColor(this, state.statusColorRes)
            binding.cardStatus.setCardBackgroundColor(color)
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.white))

            // 2. Informasi Utama
            binding.tvJenisPengajuan.text = pengajuan.jenisPengajuan
            binding.tvTargetPangkat.text = pengajuan.pangkatTujuan ?: "-"
            binding.tvTargetJabatan.text = pengajuan.jabatanTujuan ?: "-"

            // 3. Lampiran Dokumen
            lampiranAdapter.submitList(pengajuan.lampiran)
            binding.tvLampiranEmpty.visibility = if (pengajuan.lampiran.isEmpty()) View.VISIBLE else View.GONE

            // 4. Syarat Dokumen Dinamis
            val syaratText = when (pengajuan.jenisPengajuan) {
                "REGULER" -> "Wajib melampirkan: SK Pangkat dan SKP."
                "FUNGSIONAL" -> "Wajib melampirkan: SK Pangkat, SK Jabatan, SKP, dan PAK."
                "STRUKTURAL" -> "Wajib melampirkan: SK Pangkat, SK Jabatan, SK Pelantikan, SPMT, dan SKP."
                else -> "Silakan lampirkan dokumen pendukung yang sesuai."
            }
            binding.tvSyaratDokumen.text = syaratText
            binding.cardSyarat.visibility = if (state.canAttachDocuments) View.VISIBLE else View.GONE

            // 5. Pesan Admin / Catatan (Tampil hanya jika ada isi)
            if (!pengajuan.catatanVerifikator.isNullOrBlank()) {
                binding.cardCatatan.visibility = View.VISIBLE
                binding.tvCatatanVerifikator.text = pengajuan.catatanVerifikator
            } else {
                binding.cardCatatan.visibility = View.GONE
            }

            // 6. Alasan Penolakan (Tampil hanya jika ada isi)
            if (!pengajuan.alasanPenolakan.isNullOrBlank()) {
                binding.cardPenolakan.visibility = View.VISIBLE
                binding.tvAlasanPenolakan.text = pengajuan.alasanPenolakan
            } else {
                binding.cardPenolakan.visibility = View.GONE
            }

            // 7. Visibilitas Tombol Aksi
            binding.btnKelolaDokumen.visibility = if (state.canAttachDocuments) View.VISIBLE else View.GONE
            binding.btnSubmit.visibility = if (state.canSubmit) View.VISIBLE else View.GONE
            binding.btnDelete.visibility = if (state.canDelete) View.VISIBLE else View.GONE
        }
    }

    private fun showDocumentSelectionDialog() {
        val state = viewModel.uiState.value
        val documents = state.availableDocuments

        if (documents.isEmpty()) {
            Toast.makeText(this, "Belum ada dokumen. Silakan unggah di menu Dokumen.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogBinding = DialogSelectDocumentsBinding.inflate(layoutInflater)
        val adapter = SelectableDokumenAdapter(documents, state.selectedDocumentIds) { id ->
            viewModel.toggleDocumentSelection(id)
        }
        dialogBinding.rvDocuments.layoutManager = LinearLayoutManager(this)
        dialogBinding.rvDocuments.adapter = adapter

        MaterialAlertDialogBuilder(this)
            .setTitle("Kelola Lampiran Dokumen")
            .setView(dialogBinding.root)
            .setPositiveButton("Simpan") { _: DialogInterface, _: Int ->
                viewModel.attachDocuments(pengajuanId)
            }
            .setNegativeButton("Batal") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .show()
    }

    private fun showSubmitConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Submit Pengajuan")
            .setMessage("Kirim pengajuan sekarang? Data tidak dapat diubah setelah dikirim.")
            .setPositiveButton("Ya, Kirim") { _: DialogInterface, _: Int ->
                viewModel.submitPengajuan(pengajuanId)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Draf")
            .setMessage("Apakah Anda yakin ingin menghapus draf ini?")
            .setPositiveButton("Ya, Hapus") { _: DialogInterface, _: Int ->
                viewModel.deletePengajuan(pengajuanId)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}