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
        loadSummaryData()
    }

    private fun setupUI() {
        binding.btnVerifyList.setOnClickListener {
            startActivity(Intent(this, AdminPengajuanMasukActivity::class.java))
        }

        binding.btnAllHistory.setOnClickListener {
            startActivity(Intent(this, AdminAllPengajuanActivity::class.java))
        }

        binding.btnManageUsers.setOnClickListener {
            // Nanti dihubungkan ke AdminUserListActivity
            Toast.makeText(this, "Membuka Manajemen Pengguna...", Toast.LENGTH_SHORT).show()
        }

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
            } catch (e: Exception) { /* Abaikan */ }
        }
    }

    /**
     * PERBAIKAN: Memuat data ringkasan dengan validasi manual agar angka Revisi akurat
     */
    private fun loadSummaryData() {
        val token = tokenManager.getToken() ?: return
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Melakukan dua pemanggilan API secara paralel untuk sinkronisasi
                val summaryDeferred = async { RetrofitClient.instance.getAdminSummary("Bearer $token") }
                val allDataDeferred = async { RetrofitClient.instance.getAllPengajuan("Bearer $token") }

                val summaryResponse = summaryDeferred.await()
                val allResponse = allDataDeferred.await()

                if (summaryResponse.isSuccessful) {
                    val summary = summaryResponse.body()?.data
                    val listAll = allResponse.body()?.data ?: emptyList()

                    // Hitung manual status PERLU_REVISI dari list riwayat
                    val manualRevisiCount = listAll.count { it.status == "PERLU_REVISI" }

                    // Set statistik dasar
                    binding.tvTotalMasuk.text = summary?.jumlahSubmitted?.toString() ?: "0"
                    binding.tvTotalApproved.text = summary?.jumlahApproved?.toString() ?: "0"
                    binding.tvTotalRejected.text = summary?.jumlahRejected?.toString() ?: "0"

                    // Logika Koreksi: Jika backend summary mengirim 0 tapi di list /all ada data, pakai hitungan manual
                    val finalRevisiCount = if (summary?.jumlahRevisi == 0 && manualRevisiCount > 0) {
                        manualRevisiCount
                    } else {
                        summary?.jumlahRevisi ?: 0
                    }

                    binding.tvTotalRevisi.text = finalRevisiCount.toString()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminActivity, "Gagal memuat statistik dashboard", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Konfirmasi")
            .setMessage("Keluar dari aplikasi?")
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