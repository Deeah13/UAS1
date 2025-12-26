package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.sipakjabat.databinding.ActivityPegawaiBinding
import com.example.sipakjabat.utils.TokenManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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