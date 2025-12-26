package com.example.sipakjabat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.ChangePasswordRequest
import com.example.sipakjabat.data.model.UpdateProfileRequest
import com.example.sipakjabat.databinding.ActivityEditProfileBinding
import com.example.sipakjabat.databinding.DialogChangePasswordBinding
import com.example.sipakjabat.utils.TokenManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupUI()
        loadInitialData()
    }

    private fun setupUI() {
        // --- MENGHAPUS JUDUL SIPAKJABAT HITAM ---
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false) // Kita sudah punya btnBack kustom
        supportActionBar?.setDisplayShowTitleEnabled(false) // Menonaktifkan judul default aktivitas

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSimpan.setOnClickListener {
            performUpdate()
        }

        binding.btnGantiPassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun loadInitialData() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.instance.getProfile("Bearer $token")

                if (response.isSuccessful) {
                    val profile = response.body()?.data
                    profile?.let {
                        // Data header kartu
                        binding.tvProfileName.text = it.namaLengkap
                        binding.tvProfileNip.text = "NIP: ${it.nip}"

                        // Data Read-Only & Editable
                        binding.etNip.setText(it.nip)
                        binding.etJabatan.setText(it.jabatan ?: "-")
                        binding.etPangkat.setText(it.pangkatGolongan ?: "-")
                        binding.etNamaLengkap.setText(it.namaLengkap)
                        binding.etEmail.setText(it.email)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditProfileActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun performUpdate() {
        val nama = binding.etNamaLengkap.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()

        if (nama.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nama dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val request = UpdateProfileRequest(namaLengkap = nama, email = email)

        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.instance.updateProfile("Bearer $token", request)

                if (response.isSuccessful) {
                    Toast.makeText(this@EditProfileActivity, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke halaman profil
                } else {
                    Toast.makeText(this@EditProfileActivity, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showChangePasswordDialog() {
        // Menggunakan View Binding untuk dialog agar tidak ada error 'Cannot infer type'
        val dialogBinding = DialogChangePasswordBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(this)
            .setTitle("Ganti Password Akun")
            .setView(dialogBinding.root)
            .setPositiveButton("Simpan Perubahan") { _, _ ->
                val oldPass = dialogBinding.etOldPassword.text.toString().trim()
                val newPass = dialogBinding.etNewPassword.text.toString().trim()

                if (oldPass.isNotEmpty() && newPass.isNotEmpty()) {
                    performChangePassword(oldPass, newPass)
                } else {
                    Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performChangePassword(old: String, new: String) {
        val request = ChangePasswordRequest(oldPassword = old, newPassword = new)

        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.instance.changePassword("Bearer $token", request)

                if (response.isSuccessful) {
                    MaterialAlertDialogBuilder(this@EditProfileActivity)
                        .setTitle("Berhasil")
                        .setMessage("Password Anda telah diperbarui.")
                        .setPositiveButton("Selesai", null)
                        .show()
                } else {
                    Toast.makeText(this@EditProfileActivity, "Gagal: Password lama salah", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { /* handle error */ }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSimpan.isEnabled = !isLoading
    }
}