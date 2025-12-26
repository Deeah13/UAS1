package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPegawaiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        // Redirect ke login jika token tidak ditemukan
        if (tokenManager.getToken() == null) {
            redirectToLogin()
            return
        }

        setupUI()
    }

    override fun onResume() {
        super.onResume()
        // Memuat ulang profil setiap kali kembali ke halaman ini (misal: setelah edit profil)
        loadUserProfile()
    }

    private fun setupUI() {
        // Navigasi ke Riwayat Pengajuan
        binding.btnMenuPengajuan.setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }

        // Navigasi ke Kelola Dokumen
        binding.btnMenuDokumen.setOnClickListener {
            startActivity(Intent(this, DokumenActivity::class.java))
        }

        // Navigasi ke fitur Edit Profil saat Nama di-klik
        binding.tvWelcomeName.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // PERBAIKAN: Menampilkan dialog konfirmasi saat tombol logout ditekan
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    /**
     * Menampilkan Dialog Konfirmasi Logout
     */
    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                // Jika user yakin, hapus token dan pindah ke halaman Login
                tokenManager.clear()
                redirectToLogin()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                // Jika user batal, cukup tutup dialog
                dialog.dismiss()
            }
            .show()
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.instance.getProfile("Bearer $token")

                if (response.isSuccessful) {
                    val profile = response.body()?.data
                    profile?.let {
                        // Sinkronisasi dengan properti di ProfileResponse.kt
                        binding.tvWelcomeName.text = "Halo, ${it.namaLengkap}"
                        binding.tvUserNip.text = "NIP: ${it.nip}"
                        binding.tvUserJabatan.text = "Jabatan: ${it.jabatan ?: "-"}"
                        // Menampilkan Pangkat/Golongan yang diambil dari backend
                        binding.tvUserPangkat.text = "Pangkat/Gol: ${it.pangkatGolongan ?: "-"}"
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(this@PegawaiActivity, "Sesi berakhir, silakan login kembali", Toast.LENGTH_SHORT).show()
                    redirectToLogin()
                } else {
                    Toast.makeText(this@PegawaiActivity, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PegawaiActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}