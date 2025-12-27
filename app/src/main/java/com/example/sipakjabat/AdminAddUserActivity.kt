package com.example.sipakjabat

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.AdminCreateUserRequest
import com.example.sipakjabat.databinding.ActivityAdminAddUserBinding
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminAddUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAddUserBinding
    private lateinit var tokenManager: TokenManager
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAddUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        setupUI()
    }

    private fun setupUI() {
        // 1. Setup Dropdown Role (Sesuai Enum Role di Backend)
        val roles = arrayOf("PEGAWAI", "VERIFIKATOR")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, roles)
        binding.spinnerAddRole.setAdapter(adapter)

        // 2. Setup DatePicker untuk TMT Pangkat
        binding.etAddTmt.setOnClickListener { showDatePicker() }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSaveUser.setOnClickListener { validateAndSave() }
    }

    /**
     * Menampilkan Kalender untuk memilih tanggal TMT Pangkat
     */
    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                // Format yyyy-MM-dd agar sesuai dengan LocalDate di Backend
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                binding.etAddTmt.setText(format.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun validateAndSave() {
        val nip = binding.etAddNip.text.toString().trim()
        val nama = binding.etAddNama.text.toString().trim()
        val email = binding.etAddEmail.text.toString().trim()
        val password = binding.etAddPassword.text.toString().trim()
        val jabatan = binding.etAddJabatan.text.toString().trim()
        val pangkat = binding.etAddPangkat.text.toString().trim()
        val tmt = binding.etAddTmt.text.toString().trim()
        val role = binding.spinnerAddRole.text.toString()

        // Validasi field wajib sesuai aturan backend
        if (nip.isEmpty() || nama.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty() || tmt.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi seluruh data pegawai", Toast.LENGTH_SHORT).show()
            return
        }

        // Minimal password 6 karakter sesuai pengecekan di UserService backend
        if (password.length < 6) {
            Toast.makeText(this, "Password minimal harus 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        val request = AdminCreateUserRequest(
            namaLengkap = nama,
            nip = nip,
            email = email,
            password = password,
            pangkatGolongan = pangkat,
            jabatan = jabatan,
            tmtPangkatTerakhir = tmt,
            role = role.uppercase() // Mengirim role sebagai String Uppercase (PEGAWAI/VERIFIKATOR)
        )

        saveUser(request)
    }

    private fun saveUser(request: AdminCreateUserRequest) {
        val token = tokenManager.getToken() ?: return
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Endpoint Admin: POST api/admin/users
                val response = RetrofitClient.instance.createUser("Bearer $token", request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminAddUserActivity, "Akun Pegawai Berhasil Dibuat", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "NIP atau Email sudah digunakan"
                    Toast.makeText(this@AdminAddUserActivity, "Gagal: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminAddUserActivity, "Error Koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}