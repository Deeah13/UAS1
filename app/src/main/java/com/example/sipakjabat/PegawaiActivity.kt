package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.databinding.ActivityPegawaiBinding
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.launch

class PegawaiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPegawaiBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPegawaiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        // Redirect to login if token is missing
        if (tokenManager.getToken() == null) {
            redirectToLogin()
            return
        }

        setupUI()
        loadUserProfile()
    }

    private fun setupUI() {
        binding.btnMenuPengajuan.setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }

        binding.btnMenuDokumen.setOnClickListener {
            startActivity(Intent(this, DokumenActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            tokenManager.clear()
            redirectToLogin()
        }
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken()!!
                val response = RetrofitClient.instance.getProfile("Bearer $token")

                if (response.isSuccessful) {
                    val profile = response.body()?.data
                    profile?.let {
                        binding.tvWelcomeName.text = "Halo, ${it.namaLengkap}"
                        binding.tvUserNip.text = "NIP: ${it.nip}"
                        binding.tvUserJabatan.text = "Jabatan: ${it.jabatan ?: "-"}"
                    }
                } else if (response.code() == 401) {
                    // Unauthorized, token might be expired
                    Toast.makeText(this@PegawaiActivity, "Sesi berakhir, silakan login kembali", Toast.LENGTH_LONG).show()
                    tokenManager.clear()
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