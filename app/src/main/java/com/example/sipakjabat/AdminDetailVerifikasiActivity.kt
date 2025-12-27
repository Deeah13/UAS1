package com.example.sipakjabat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.VerifikasiRequest
import com.example.sipakjabat.data.model.RevisiRequest
import com.example.sipakjabat.databinding.ActivityAdminDetailVerifikasiBinding
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.launch

class AdminDetailVerifikasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDetailVerifikasiBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var lampiranAdapter: DokumenLampiranAdapter
    private var pengajuanId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDetailVerifikasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        pengajuanId = intent.getLongExtra("PENGAJUAN_ID", -1)

        setupRecyclerView()
        binding.btnBack.setOnClickListener { finish() }

        setupActionListeners()
        loadDetailData()
    }

    private fun setupRecyclerView() {
        lampiranAdapter = DokumenLampiranAdapter()
        binding.rvLampiranVerifikasi.apply {
            layoutManager = LinearLayoutManager(this@AdminDetailVerifikasiActivity)
            adapter = lampiranAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadDetailData() {
        val token = tokenManager.getToken() ?: return
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getAdminDetailPengajuan("Bearer $token", pengajuanId)
                if (response.isSuccessful) {
                    val it = response.body()?.data
                    it?.let {
                        // 1. Data Identitas & Pengajuan
                        binding.tvNamaDetail.text = "Nama: ${it.user?.namaLengkap ?: "-"}"
                        binding.tvNipDetail.text = "NIP: ${it.user?.nip ?: "-"}"
                        binding.tvEmailDetail.text = "Email: ${it.user?.email ?: "-"}"
                        binding.tvJenisDetail.text = "Jenis: ${it.jenisPengajuan}"
                        binding.tvPangkatSaatIni.text = "Pangkat Sekarang: ${it.pangkatSaatIni ?: "-"}"
                        binding.tvJabatanSaatIni.text = "Jabatan Sekarang: ${it.jabatanSaatIni ?: "-"}"
                        binding.tvPangkatTujuan.text = "Pangkat Tujuan: ${it.pangkatTujuan ?: "-"}"
                        binding.tvJabatanTujuan.text = "Jabatan Tujuan: ${it.jabatanTujuan ?: "-"}"

                        // 2. Data Lampiran
                        lampiranAdapter.submitList(it.lampiran)
                        binding.tvLampiranEmpty.visibility = if (it.lampiran.isEmpty()) View.VISIBLE else View.GONE

                        // 3. LOGIKA HISTORY vs ACTION
                        if (it.status == "SUBMITTED") {
                            // Masih bisa diverifikasi
                            binding.cardHasilVerifikasi.visibility = View.GONE
                            setActionsVisibility(true)
                        } else {
                            // Sudah selesai diproses, tampilkan Riwayat
                            setActionsVisibility(false)
                            binding.cardHasilVerifikasi.visibility = View.VISIBLE
                            updateHistoryCard(it.status, it.catatanVerifikator)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminDetailVerifikasiActivity, "Kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateHistoryCard(status: String, catatan: String?) {
        binding.tvStatusFinal.text = "STATUS: $status"
        binding.tvCatatanHistory.text = "Catatan: ${catatan ?: "(Tidak ada catatan)"}"

        val colorRes = when (status) {
            "APPROVED" -> android.R.color.holo_green_dark
            "REJECTED" -> android.R.color.holo_red_dark
            "PERLU_REVISI" -> android.R.color.holo_orange_dark
            else -> android.R.color.darker_gray
        }

        val color = ContextCompat.getColor(this, colorRes)
        binding.tvStatusFinal.setTextColor(color)
        binding.tvLabelHasil.setTextColor(color)
        binding.cardHasilVerifikasi.setStrokeColor(color)
    }

    private fun setActionsVisibility(isVisible: Boolean) {
        val vis = if (isVisible) View.VISIBLE else View.GONE
        binding.tilCatatan.visibility = vis
        binding.btnApprove.visibility = vis
        binding.btnRevisi.visibility = vis
        binding.btnReject.visibility = vis
    }

    private fun setupActionListeners() {
        binding.btnApprove.setOnClickListener { prosesVerifikasi("APPROVE") }
        binding.btnRevisi.setOnClickListener { prosesVerifikasi("REVISI") }
        binding.btnReject.setOnClickListener { prosesVerifikasi("REJECT") }
    }

    private fun prosesVerifikasi(aksi: String) {
        val catatan = binding.etCatatan.text.toString().trim()
        val token = tokenManager.getToken() ?: return

        if ((aksi == "REJECT" || aksi == "REVISI") && catatan.isEmpty()) {
            Toast.makeText(this, "Catatan wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = when (aksi) {
                    "APPROVE" -> RetrofitClient.instance.approvePengajuan("Bearer $token", pengajuanId, VerifikasiRequest(catatan))
                    "REJECT" -> RetrofitClient.instance.rejectPengajuan("Bearer $token", pengajuanId, VerifikasiRequest(catatan))
                    else -> RetrofitClient.instance.mintaRevisi("Bearer $token", pengajuanId, RevisiRequest(catatan))
                }
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminDetailVerifikasiActivity, "Berhasil diproses", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminDetailVerifikasiActivity, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}