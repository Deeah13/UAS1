package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.databinding.ActivityPegawaiBinding
import com.example.sipakjabat.utils.TokenManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class PegawaiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPegawaiBinding
    private lateinit var tokenManager: TokenManager
    private var isMenuOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPegawaiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        if (tokenManager.getToken() == null) {
            redirectToLogin()
            return
        }

        setupUI()
        // Memuat data saat pertama kali aplikasi dibuka
        loadDashboardData()
    }

    // Memastikan data diperbarui setiap kali pengguna kembali ke Dashboard
    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun setupUI() {
        // Klik logo untuk buka/tutup menu
        binding.mainLogoTrigger.setOnClickListener {
            toggleMenu()
        }

        // Navigasi satelit
        binding.btnMenuProfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnMenuPengajuan.setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }

        binding.btnMenuDokumen.setOnClickListener {
            startActivity(Intent(this, DokumenActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    /**
     * Mengambil data pengajuan dari backend dan menghitung jumlah berdasarkan status.
     * Menggunakan pemetaan status sesuai Enum di Backend (StatusPengajuan.java).
     */
    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.instance.getRiwayatPengajuan("Bearer $token")

                if (response.isSuccessful) {
                    val dataList = response.body()?.data ?: emptyList()

                    // Menghitung status dengan menyamakan case (uppercase) agar akurat
                    // 1. PROSES: Gabungan dari DRAFT, SUBMITTED (menunggu), dan PERLU_REVISI
                    val countProses = dataList.count {
                        val s = it.status.uppercase()
                        s == "SUBMITTED" || s == "PERLU_REVISI" || s == "DRAFT"
                    }

                    // 2. DITERIMA: Sesuai status APPROVED di backend
                    val countDiterima = dataList.count {
                        it.status.uppercase() == "APPROVED"
                    }

                    // 3. DITOLAK: Sesuai status REJECTED di backend
                    val countDitolak = dataList.count {
                        it.status.uppercase() == "REJECTED"
                    }

                    // Memperbarui teks pada kartu ringkasan di UI
                    binding.tvCountPending.text = countProses.toString()
                    binding.tvCountDiterima.text = countDiterima.toString()
                    binding.tvCountDitolak.text = countDitolak.toString()
                }
            } catch (e: Exception) {
                // Jika terjadi kesalahan koneksi, angka tetap pada nilai default (0)
            }
        }
    }

    private fun toggleMenu() {
        isMenuOpen = !isMenuOpen
        val targetAlpha = if (isMenuOpen) 1f else 0f
        val targetScale = if (isMenuOpen) 1f else 0f

        val menuViews: List<View> = listOf(
            binding.btnMenuProfil,
            binding.btnMenuPengajuan,
            binding.btnMenuDokumen,
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
                .setDuration(400L + (index * 100L))
                .setInterpolator(android.view.animation.OvershootInterpolator(1.3f))
                .withEndAction {
                    if (!isMenuOpen) view.visibility = View.INVISIBLE
                }
                .start()
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
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