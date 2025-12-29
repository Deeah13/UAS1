package com.example.sipakjabat

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.ChangePasswordRequest
import com.example.sipakjabat.databinding.ActivityProfileBinding
import com.example.sipakjabat.databinding.DialogChangePasswordBinding
import com.example.sipakjabat.utils.TokenManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var tokenManager: TokenManager

    override fun onResume() {
        super.onResume()
        loadProfileData() // Memastikan data terbaru dimuat saat kembali dari Edit Profil
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupToolbar()
        setupButtons()
        loadProfileData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupButtons() {
        // Navigasi ke halaman Edit Profile
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Menampilkan dialog ganti password
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun loadProfileData() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.instance.getProfile("Bearer $token")

                if (response.isSuccessful) {
                    val profile = response.body()?.data
                    profile?.let {
                        // Menampilkan data pegawai ke komponen UI
                        binding.tvProfileName.text = it.namaLengkap
                        binding.tvProfileNip.text = "NIP: ${it.nip}"
                        binding.tvProfileJabatan.text = it.jabatan ?: "-"
                        binding.tvProfilePangkat.text = it.pangkatGolongan ?: "-"
                        binding.tvProfileEmail.text = it.email ?: "-" // Menampilkan email
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Gagal memuat profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogBinding = DialogChangePasswordBinding.inflate(layoutInflater)

        val titleView = TextView(this).apply {
            text = "Ganti Password"
            textSize = 20f
            setPadding(64, 48, 64, 0)
            setTextColor(Color.parseColor("#014488"))
            gravity = Gravity.START
            typeface = ResourcesCompat.getFont(this@ProfileActivity, R.font.poppins_bold)
        }

        MaterialAlertDialogBuilder(this)
            .setCustomTitle(titleView)
            .setView(dialogBinding.root)
            .setPositiveButton("Simpan") { _, _ ->
                val oldPass = dialogBinding.etOldPassword.text.toString()
                val newPass = dialogBinding.etNewPassword.text.toString()
                if (oldPass.isNotEmpty() && newPass.isNotEmpty()) {
                    performChangePassword(oldPass, newPass)
                } else {
                    Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performChangePassword(old: String, new: String) {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch

                // Mengirim request dengan variabel yang sesuai dengan backend (currentPassword & newPassword)
                val request = ChangePasswordRequest(currentPassword = old, newPassword = new)
                val response = RetrofitClient.instance.changePassword("Bearer $token", request)

                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Password berhasil diganti!", Toast.LENGTH_SHORT).show()
                } else {
                    // Menangani error jika password lama salah atau tidak sesuai kriteria
                    Toast.makeText(this@ProfileActivity, "Gagal: Password lama mungkin salah", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Terjadi kesalahan koneksi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}