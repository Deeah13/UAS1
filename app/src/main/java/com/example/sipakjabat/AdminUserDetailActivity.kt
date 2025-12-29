package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.UbahRoleRequestDTO
import com.example.sipakjabat.databinding.ActivityAdminUserDetailBinding
import com.example.sipakjabat.utils.TokenManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class AdminUserDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminUserDetailBinding
    private lateinit var tokenManager: TokenManager
    private var userId: Long = -1

    // Variabel penampung data untuk dikirim ke halaman Edit
    private var currentNip: String = ""
    private var currentNama: String = ""
    private var currentEmail: String = ""
    private var currentPangkat: String = ""
    private var currentJabatan: String = ""
    private var currentTmt: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        userId = intent.getLongExtra("USER_ID", -1)

        if (userId == -1L) {
            Toast.makeText(this, "ID Pengguna tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadUserDetail()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // FITUR EDIT DATA:
        binding.btnEditUser.setOnClickListener {
            val intent = Intent(this, AdminEditUserActivity::class.java).apply {
                putExtra("USER_ID", userId)
                putExtra("NIP", currentNip)
                putExtra("NAMA", currentNama)
                putExtra("EMAIL", currentEmail)
                putExtra("PANGKAT", currentPangkat)
                putExtra("JABATAN", currentJabatan)
                putExtra("TMT", currentTmt)
            }
            startActivity(intent)
        }

        binding.btnUbahRole.setOnClickListener { showRoleDialog() }

        binding.btnAddDokumen.setOnClickListener {
            val intent = Intent(this, AdminAddUserDokumenActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        binding.btnDeleteUser.setOnClickListener { showDeleteConfirmation() }
    }

    private fun loadUserDetail() {
        val token = tokenManager.getToken() ?: return
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Backend mengembalikan objek User yang berisi field nip
                val response = RetrofitClient.instance.getUserDetail("Bearer $token", userId)
                if (response.isSuccessful) {
                    val user = response.body()?.data
                    user?.let {
                        // Simpan ke variabel lokal agar bisa dikirim ke form edit
                        currentNip = it.nip ?: ""
                        currentNama = it.namaLengkap
                        currentEmail = it.email
                        currentPangkat = it.pangkatGolongan ?: ""
                        currentJabatan = it.jabatan ?: ""
                        currentTmt = it.tmtPangkatTerakhir ?: ""

                        // Tampilkan ke UI
                        binding.tvDetailNama.text = it.namaLengkap
                        binding.tvDetailNip.text = "NIP: ${currentNip.ifEmpty { "-" }}"
                        binding.tvDetailEmail.text = "Email: ${it.email}"
                        binding.tvDetailRole.text = it.role.toString()
                        binding.tvDetailPangkat.text = "Pangkat: ${currentPangkat.ifEmpty { "-" }}"
                        binding.tvDetailJabatan.text = "Jabatan: ${currentJabatan.ifEmpty { "-" }}"
                        binding.tvDetailTmt.text = "TMT Pangkat: ${currentTmt.ifEmpty { "-" }}"
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminUserDetailActivity, "Gagal memuat detail", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showRoleDialog() {
        val roles = arrayOf("PEGAWAI", "VERIFIKATOR")
        MaterialAlertDialogBuilder(this)
            .setTitle("Ubah Role Pengguna")
            .setItems(roles) { _, which ->
                updateUserRole(roles[which])
            }
            .show()
    }

    private fun updateUserRole(newRole: String) {
        val token = tokenManager.getToken() ?: return
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val request = UbahRoleRequestDTO(newRole = newRole.uppercase())
                val response = RetrofitClient.instance.ubahRole("Bearer $token", userId, request)

                if (response.isSuccessful) {
                    Toast.makeText(this@AdminUserDetailActivity, "Role berhasil diubah ke $newRole", Toast.LENGTH_SHORT).show()
                    loadUserDetail()
                } else {
                    Toast.makeText(this@AdminUserDetailActivity, "Gagal mengubah role", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminUserDetailActivity, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Pengguna")
            .setMessage("Akun ini akan dihapus permanen beserta seluruh data terkait. Lanjutkan?")
            .setPositiveButton("Hapus") { _, _ -> deleteUser() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteUser() {
        val token = tokenManager.getToken() ?: return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.deleteUser("Bearer $token", userId)
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminUserDetailActivity, "Pengguna berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AdminUserDetailActivity, "Gagal menghapus pengguna", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminUserDetailActivity, "Gagal menghapus", Toast.LENGTH_SHORT).show()
            }
        }
    }
}