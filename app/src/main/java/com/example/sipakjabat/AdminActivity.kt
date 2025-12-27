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
    private var isMenuOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        if (tokenManager.getToken() == null) {
            redirectToLogin()
            return
        }

        setupUI()
    }

    override fun onResume() {
        super.onResume()
        // Memuat ulang statistik dan menyapa admin
        loadDashboardData()
    }

    private fun setupUI() {
        // Klik logo untuk memicu animasi mirror
        binding.mainLogoTrigger.setOnClickListener {
            toggleRadialMenu()
        }

        // Navigasi ke Profil (Sama seperti Pegawai)
        binding.btnMenuProfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnMenuVerifikasi.setOnClickListener {
            startActivity(Intent(this, AdminPengajuanMasukActivity::class.java))
        }

        binding.btnMenuRiwayat.setOnClickListener {
            startActivity(Intent(this, AdminAllPengajuanActivity::class.java))
        }

        binding.btnMenuUsers.setOnClickListener {
            startActivity(Intent(this, AdminUserListActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    /**
     * Mengambil data profil dan ringkasan statistik status pengajuan
     */
    private fun loadDashboardData() {
        val token = tokenManager.getToken() ?: return
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Mengambil summary dan daftar lengkap secara asinkron untuk validasi data revisi
                val summaryDef = async { RetrofitClient.instance.getAdminSummary("Bearer $token") }
                val allDataDef = async { RetrofitClient.instance.getAllPengajuan("Bearer $token") }

                val summaryRes = summaryDef.await()
                val allRes = allDataDef.await()

                if (summaryRes.isSuccessful) {
                    val summary = summaryRes.body()?.data
                    val listAll = allRes.body()?.data ?: emptyList()
                    val manualRevisiCount = listAll.count { it.status == "PERLU_REVISI" }

                    // Update UI Statistik di Kartu Tengah
                    binding.tvTotalMasuk.text = summary?.jumlahSubmitted?.toString() ?: "0"
                    binding.tvTotalApproved.text = summary?.jumlahApproved?.toString() ?: "0"
                    binding.tvTotalRejected.text = summary?.jumlahRejected?.toString() ?: "0"

                    // Gunakan validasi manual jika backend memberikan angka 0 padahal ada data
                    binding.tvTotalRevisi.text = if (summary?.jumlahRevisi == 0 && manualRevisiCount > 0) {
                        manualRevisiCount.toString()
                    } else {
                        summary?.jumlahRevisi?.toString() ?: "0"
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminActivity, "Gagal sinkronisasi dashboard", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun toggleRadialMenu() {
        isMenuOpen = !isMenuOpen
        val targetAlpha = if (isMenuOpen) 1f else 0f
        val targetScale = if (isMenuOpen) 1f else 0f

        val menuViews = listOf(
            binding.btnMenuProfil,
            binding.btnMenuVerifikasi,
            binding.btnMenuRiwayat,
            binding.btnMenuUsers,
            binding.btnLogout
        )

        menuViews.forEachIndexed { index, view ->
            if (isMenuOpen) {
                view.visibility = View.VISIBLE
                view.scaleX = 0f
                view.scaleY = 0f
            }

            view.animate()
                .alpha(targetAlpha)
                .scaleX(targetScale)
                .scaleY(targetScale)
                .setDuration(450L + (index * 80L))
                .setInterpolator(android.view.animation.OvershootInterpolator(1.4f))
                .withEndAction {
                    if (!isMenuOpen) view.visibility = View.INVISIBLE
                }
                .start()
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin keluar dari sesi Administrator?")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                tokenManager.clear()
                redirectToLogin()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}