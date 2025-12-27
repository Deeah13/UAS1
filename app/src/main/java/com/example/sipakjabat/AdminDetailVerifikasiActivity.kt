package com.example.sipakjabat

import android.graphics.Color
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

        // Listener tombol kembali
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
                        // 1. DATA IDENTITAS (Box Biru Muda)
                        // Hanya mengisi data karena label statis sudah ada di XML
                        binding.tvNamaDetail.text = it.user?.namaLengkap ?: "-"
                        binding.tvNipDetail.text = "NIP: ${it.user?.nip ?: "-"}"
                        binding.tvEmailDetail.text = "Email: ${it.user?.email ?: "-"}"

                        // 2. RINCIAN KENAIKAN (Box Cream - REFINED)
                        // Hierarki: Hanya isi data, label tebal ada di XML
                        binding.tvJenisDetail.text = it.jenisPengajuan
                        binding.tvPangkatSaatIni.text = it.pangkatSaatIni ?: "-"
                        binding.tvJabatanSaatIni.text = it.jabatanSaatIni ?: "-"
                        binding.tvPangkatTujuan.text = it.pangkatTujuan ?: "-"
                        binding.tvJabatanTujuan.text = it.jabatanTujuan ?: "-"

                        // 3. DATA LAMPIRAN
                        lampiranAdapter.submitList(it.lampiran)
                        binding.tvLampiranEmpty.visibility = if (it.lampiran.isEmpty()) View.VISIBLE else View.GONE

                        // 4. LOGIKA VISIBILITAS (Verifikasi Aktif vs Riwayat Selesai)
                        if (it.status == "SUBMITTED") {
                            binding.cardHasilVerifikasi.visibility = View.GONE
                            setActionsVisibility(true)
                        } else {
                            setActionsVisibility(false)
                            binding.cardHasilVerifikasi.visibility = View.VISIBLE
                            updateHistoryCard(it.status, it.catatanVerifikator)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminDetailVerifikasiActivity, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    /**
     * Update UI Riwayat Verifikasi: Background Clear dengan Outline berwarna sesuai status
     */
    private fun updateHistoryCard(status: String, catatan: String?) {
        val statusClean = when (status) {
            "APPROVED" -> "SETUJU"
            "REJECTED" -> "DITOLAK"
            "PERLU_REVISI" -> "REVISI"
            else -> status
        }

        binding.tvStatusFinal.text = "STATUS: $statusClean"
        binding.tvCatatanHistory.text = "Catatan: ${catatan ?: "(Tidak ada catatan)"}"

        val colorHex = when (status) {
            "APPROVED" -> "#2E7D32"    // Hijau
            "REJECTED" -> "#DC2626"    // Merah
            "PERLU_REVISI" -> "#C5A059" // Gold
            else -> "#718096"          // Abu-abu
        }

        val colorInt = Color.parseColor(colorHex)

        // Atur Style Card: Background Clear (Putih), Outline Berwarna
        binding.cardHasilVerifikasi.setCardBackgroundColor(Color.WHITE)
        binding.cardHasilVerifikasi.strokeColor = colorInt
        binding.cardHasilVerifikasi.strokeWidth = 4 // Bingkai tegas

        // Atur warna teks agar senada dengan outline
        binding.tvStatusFinal.setTextColor(colorInt)
        binding.tvLabelHasil.setTextColor(colorInt)
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

        // Validasi catatan untuk penolakan atau revisi
        if ((aksi == "REJECT" || aksi == "REVISI") && catatan.isEmpty()) {
            Toast.makeText(this, "Catatan wajib diisi untuk tindakan ini!", Toast.LENGTH_SHORT).show()
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
                } else {
                    Toast.makeText(this@AdminDetailVerifikasiActivity, "Gagal memproses pengajuan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminDetailVerifikasiActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}