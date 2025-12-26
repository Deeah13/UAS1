package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    // ProfileActivity.kt (Jika ada)
    override fun onResume() {
        super.onResume()
        loadProfileData() // Pastikan nama fungsi sesuai dengan fungsi pengambil API Anda
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
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupButtons() {
        // Navigasi ke EditProfileActivity
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Memanggil dialog Change Password
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
                        binding.tvProfileName.text = it.namaLengkap
                        binding.tvProfileNip.text = "NIP: ${it.nip}"
                        binding.tvProfileJabatan.text = it.jabatan ?: "-"
                        binding.tvProfilePangkat.text = it.pangkatGolongan ?: "-"
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogBinding = DialogChangePasswordBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(this)
            .setTitle("Ganti Password")
            .setView(dialogBinding.root)
            .setPositiveButton("Simpan") { _, _ ->
                val oldPass = dialogBinding.etOldPassword.text.toString()
                val newPass = dialogBinding.etNewPassword.text.toString()
                if (oldPass.isNotEmpty() && newPass.isNotEmpty()) {
                    performChangePassword(oldPass, newPass)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performChangePassword(old: String, new: String) {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val request = ChangePasswordRequest(old, new)
                val response = RetrofitClient.instance.changePassword("Bearer $token", request)
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Password diganti!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { /* handle error */ }
        }
    }
}