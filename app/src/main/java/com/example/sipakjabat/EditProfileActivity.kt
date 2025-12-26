package com.example.sipakjabat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.UpdateProfileRequest
import com.example.sipakjabat.databinding.ActivityEditProfileBinding
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupToolbar()
        loadCurrentProfile()

        binding.btnSimpan.setOnClickListener {
            performUpdateProfile()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Fungsi kembali melalui ImageButton manual
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadCurrentProfile() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.instance.getProfile("Bearer $token")
                if (response.isSuccessful) {
                    val profile = response.body()?.data
                    profile?.let {
                        binding.tvProfileName.text = it.namaLengkap
                        binding.tvProfileNip.text = "NIP: ${it.nip}"
                        binding.etNip.setText(it.nip)
                        binding.etPangkat.setText(it.pangkatGolongan ?: "-")
                        binding.etJabatan.setText(it.jabatan ?: "-")
                        binding.etNamaLengkap.setText(it.namaLengkap)
                        binding.etEmail.setText(it.email)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditProfileActivity, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performUpdateProfile() {
        val nama = binding.etNamaLengkap.text.toString()
        val email = binding.etEmail.text.toString()

        if (nama.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nama dan Email wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSimpan.isEnabled = false
            try {
                val token = tokenManager.getToken() ?: return@launch
                val request = UpdateProfileRequest(namaLengkap = nama, email = email)
                val response = RetrofitClient.instance.updateProfile("Bearer $token", request)

                if (response.isSuccessful) {
                    Toast.makeText(this@EditProfileActivity, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke halaman profil
                } else {
                    Toast.makeText(this@EditProfileActivity, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditProfileActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSimpan.isEnabled = true
            }
        }
    }
}