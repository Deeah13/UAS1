package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
import android.view.View
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
        loadDashboardData()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun setupUI() {
        binding.mainLogoTrigger.setOnClickListener { toggleMenu() }
        binding.btnMenuProfil.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.btnMenuPengajuan.setOnClickListener { startActivity(Intent(this, RiwayatActivity::class.java)) }
        binding.btnMenuDokumen.setOnClickListener { startActivity(Intent(this, DokumenActivity::class.java)) }
        binding.btnLogout.setOnClickListener { showLogoutConfirmationDialog() }
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.instance.getRiwayatPengajuan("Bearer $token")

                if (response.isSuccessful) {
                    val dataList = response.body()?.data ?: emptyList()

                    // PERBAIKAN LOGIKA: Statistik "Proses" hanya Submitted dan Perlu Revisi
                    val countProses = dataList.count {
                        val s = it.status.uppercase()
                        s == "SUBMITTED" || s == "PERLU_REVISI"
                    }

                    val countDiterima = dataList.count { it.status.uppercase() == "APPROVED" }
                    val countDitolak = dataList.count { it.status.uppercase() == "REJECTED" }

                    // Perbarui UI Statistik Sebaris
                    binding.tvCountPending.text = countProses.toString()
                    binding.tvCountDiterima.text = countDiterima.toString()
                    binding.tvCountDitolak.text = countDitolak.toString()
                }
            } catch (e: Exception) {
                // Biarkan angka tetap 0 jika error koneksi
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
                .withEndAction { if (!isMenuOpen) view.visibility = View.INVISIBLE }
                .start()
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                tokenManager.clear() // PERBAIKAN: Menggunakan clear() sesuai TokenManager.kt
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