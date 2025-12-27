package com.example.sipakjabat

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.UpdateUserByAdminRequest
import com.example.sipakjabat.databinding.ActivityAdminEditUserBinding
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.launch
import java.util.*
import java.text.SimpleDateFormat

class AdminEditUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminEditUserBinding
    private lateinit var tokenManager: TokenManager
    private var userId: Long = -1
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminEditUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        userId = intent.getLongExtra("USER_ID", -1)

        if (userId == -1L) {
            Toast.makeText(this, "ID Pengguna tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 1. Populasi Data Awal dari Intent (Termasuk NIP untuk field Read-Only)
        populateInitialData()

        // 2. Inisialisasi Listener Tombol dan Input
        setupListeners()
    }

    private fun populateInitialData() {
        // Mengambil NIP yang dikirim dari AdminUserDetailActivity
        val nip = intent.getStringExtra("NIP")
        binding.etEditNip.setText(nip ?: "-")

        // Mengisi data lainnya ke input field
        binding.etEditNama.setText(intent.getStringExtra("NAMA"))
        binding.etEditEmail.setText(intent.getStringExtra("EMAIL"))
        binding.etEditPangkat.setText(intent.getStringExtra("PANGKAT"))
        binding.etEditJabatan.setText(intent.getStringExtra("JABATAN"))
        binding.etEditTmt.setText(intent.getStringExtra("TMT"))
    }

    private fun setupListeners() {
        // Tombol Kembali
        binding.btnBack.setOnClickListener { finish() }

        // Konfigurasi Input Tanggal (TMT) agar memunculkan kalender
        binding.etEditTmt.setOnClickListener { showDatePicker() }
        binding.etEditTmt.isFocusable = false // Menjaga format agar tetap YYYY-MM-DD

        // Tombol Simpan Perubahan
        binding.btnUpdateUser.setOnClickListener { performUpdate() }
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            binding.etEditTmt.setText(format.format(calendar.time))
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun performUpdate() {
        // 1. Ambil data dari Input Field
        val nama = binding.etEditNama.text.toString().trim()
        val email = binding.etEditEmail.text.toString().trim()
        val pangkat = binding.etEditPangkat.text.toString().trim()
        val jabatan = binding.etEditJabatan.text.toString().trim()
        val tmt = binding.etEditTmt.text.toString().trim()

        // 2. Validasi Sederhana
        if (nama.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nama dan Email wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val request = UpdateUserByAdminRequest(
            namaLengkap = nama,
            email = email,
            pangkatGolongan = pangkat,
            jabatan = jabatan,
            tmtPangkatTerakhir = tmt // Dikirim dalam format string YYYY-MM-DD
        )

        val token = tokenManager.getToken()
        if (token == null) {
            Toast.makeText(this, "Sesi habis, silakan login kembali", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Proses API dengan State Loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnUpdateUser.isEnabled = false

        lifecycleScope.launch {
            try {
                // Memanggil endpoint PUT /api/admin/users/{id}
                val response = RetrofitClient.instance.updateUser("Bearer $token", userId, request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminEditUserActivity, "Data Pegawai Berhasil Diperbarui", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke Profil Pegawai
                } else {
                    val msg = response.errorBody()?.string() ?: "Gagal memperbarui data"
                    Toast.makeText(this@AdminEditUserActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminEditUserActivity, "Kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnUpdateUser.isEnabled = true
            }
        }
    }
}