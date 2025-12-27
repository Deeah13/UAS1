package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.databinding.ActivityAdminBinding
import com.example.sipakjabat.utils.TokenManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        setupUI()

        val token = tokenManager.getToken()
        if (token != null) {
            fetchProfileData("Bearer $token")
        }
    }

    override fun onResume() {
        super.onResume()
        // Mengambil data statistik terbaru setiap kali admin kembali ke Dashboard
        loadSummaryData()
    }

    private fun setupUI() {
        // 1. Navigasi ke Daftar Pengajuan Baru (Verifikasi)
        binding.btnVerifyList.setOnClickListener {
            startActivity(Intent(this, AdminPengajuanMasukActivity::class.java))
        }

        // 2. Navigasi ke Riwayat Seluruh Pengajuan
        binding.btnAllHistory.setOnClickListener {
            startActivity(Intent(this, AdminAllPengajuanActivity::class.java))
        }

        // 3. FIX: Navigasi ke Manajemen Pengguna (Daftar Pegawai)
        binding.btnManageUsers.setOnClickListener {
            // Berpindah ke halaman daftar pegawai
            val intent = Intent(this, AdminUserListActivity::class.java)
            startActivity(intent)
        }

        // 4. Konfirmasi Keluar
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun fetchProfileData(tokenWithBearer: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getProfile(tokenWithBearer)
                if (response.isSuccessful) {
                    val profile = response.body()?.data
                    binding.tvWelcome.text = "Halo, ${profile?.namaLengkap ?: "Admin"}"
                    binding.tvAdminNip.text = "NIP: ${profile?.nip ?: "-"}"
                }
            } catch (e: Exception) { /* Abaikan jika gagal */ }
        }
    }

    /**
     * Memuat data ringkasan dengan validasi manual agar angka "Revisi" akurat
     */
    private fun loadSummaryData() {
        val token = tokenManager.getToken() ?: return
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Mengambil data dari dua endpoint sekaligus untuk sinkronisasi data
                val summaryDeferred = async { RetrofitClient.instance.getAdminSummary("Bearer $token") }
                val allDataDeferred = async { RetrofitClient.instance.getAllPengajuan("Bearer $token") }

                val summaryResponse = summaryDeferred.await()
                val allResponse = allDataDeferred.await()

                if (summaryResponse.isSuccessful) {
                    val summary = summaryResponse.body()?.data
                    val listAll = allResponse.body()?.data ?: emptyList()

                    // Menghitung manual status PERLU_REVISI dari daftar riwayat sebagai validasi
                    val manualRevisiCount = listAll.count { it.status == "PERLU_REVISI" }

                    // Update UI Statistik
                    binding.tvTotalMasuk.text = summary?.jumlahSubmitted?.toString() ?: "0"
                    binding.tvTotalApproved.text = summary?.jumlahApproved?.toString() ?: "0"
                    binding.tvTotalRejected.text = summary?.jumlahRejected?.toString() ?: "0"

                    // Gunakan hitungan manual jika backend summary memberikan angka 0 padahal data ada
                    val finalRevisiCount = if (summary?.jumlahRevisi == 0 && manualRevisiCount > 0) {
                        manualRevisiCount
                    } else {
                        summary?.jumlahRevisi ?: 0
                    }

                    binding.tvTotalRevisi.text = finalRevisiCount.toString()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminActivity, "Gagal memperbarui dashboard", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Keluar") { _, _ ->
                tokenManager.clear()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}