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

        // Isi data awal
        binding.etEditNama.setText(intent.getStringExtra("NAMA"))
        binding.etEditEmail.setText(intent.getStringExtra("EMAIL"))
        binding.etEditPangkat.setText(intent.getStringExtra("PANGKAT"))
        binding.etEditJabatan.setText(intent.getStringExtra("JABATAN"))
        binding.etEditTmt.setText(intent.getStringExtra("TMT"))

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // TAMPILKAN KALENDER SAAT KLIK TMT
        binding.etEditTmt.setOnClickListener { showDatePicker() }
        binding.etEditTmt.isFocusable = false // Agar tidak bisa diketik manual

        binding.btnUpdateUser.setOnClickListener { performUpdate() }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                binding.etEditTmt.setText(format.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun performUpdate() {
        val request = UpdateUserByAdminRequest(
            namaLengkap = binding.etEditNama.text.toString().trim(),
            email = binding.etEditEmail.text.toString().trim(),
            pangkatGolongan = binding.etEditPangkat.text.toString().trim(),
            jabatan = binding.etEditJabatan.text.toString().trim(),
            tmtPangkatTerakhir = binding.etEditTmt.text.toString().trim()
        )

        if (request.namaLengkap.isEmpty() || request.email.isEmpty()) {
            Toast.makeText(this, "Nama dan Email wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val token = tokenManager.getToken() ?: return
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.updateUser("Bearer $token", userId, request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminEditUserActivity, "Data Pegawai Berhasil Diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AdminEditUserActivity, "Gagal memperbarui data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminEditUserActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}