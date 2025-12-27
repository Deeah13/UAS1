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
    }

    override fun onResume() {
        super.onResume()
        // Selalu perbarui statistik saat Admin kembali ke Dashboard
        loadSummaryData()
    }

    private fun setupUI() {
        // Tombol untuk memeriksa pengajuan yang berstatus SUBMITTED
        binding.btnVerifyList.setOnClickListener {
            val intent = Intent(this, AdminPengajuanMasukActivity::class.java)
            startActivity(intent)
        }

        binding.btnManageUsers.setOnClickListener {
            // Placeholder untuk fitur manajemen user (Tambah/Edit/Hapus Pegawai)
            Toast.makeText(this, "Fitur Manajemen User akan tersedia segera", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    /**
     * Mengambil data statistik pengajuan dari backend.
     * Menggunakan endpoint api/admin/pengajuan/summary
     */
    private fun loadSummaryData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.instance.getAdminSummary("Bearer $token")

                if (response.isSuccessful) {
                    val summary = response.body()?.data
                    // Update UI dengan data dari SummaryResponseDTO
                    binding.tvTotalMasuk.text = summary?.jumlahSubmitted?.toString() ?: "0"
                    binding.tvTotalApproved.text = summary?.jumlahApproved?.toString() ?: "0"
                    binding.tvTotalRejected.text = summary?.jumlahRejected?.toString() ?: "0"
                } else {
                    Toast.makeText(this@AdminActivity, "Gagal memuat statistik: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminActivity, "Gagal memuat statistik: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari panel admin?")
            .setIcon(R.drawable.logo_lingkaran_baru) // Menggunakan logo sipakjabat
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