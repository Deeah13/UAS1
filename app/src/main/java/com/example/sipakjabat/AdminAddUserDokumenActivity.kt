package com.example.sipakjabat

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.CreateDokumenRequest
import com.example.sipakjabat.databinding.ActivityAdminAddUserDokumenBinding
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminAddUserDokumenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAddUserDokumenBinding
    private lateinit var tokenManager: TokenManager
    private var userId: Long = -1
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAddUserDokumenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        userId = intent.getLongExtra("USER_ID", -1)

        setupUI()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        // Setup Dropdown Jenis Dokumen sesuai Enum di Backend
        val jenisList = arrayOf("SK_PANGKAT", "SKP", "SK_JABATAN", "PAK", "SK_PELANTIKAN", "SPMT")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, jenisList)
        binding.spinnerJenisDokumen.setAdapter(adapter)

        // DatePicker untuk Tanggal Terbit
        binding.etTanggalTerbit.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(y, m, d)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                binding.etTanggalTerbit.setText(sdf.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnSimpanDokumen.setOnClickListener { validateAndSave() }
    }

    private fun validateAndSave() {
        val jenis = binding.spinnerJenisDokumen.text.toString()
        val nomor = binding.etNomorDokumen.text.toString().trim()
        val tgl = binding.etTanggalTerbit.text.toString()
        val desk = binding.etDeskripsi.text.toString().trim()

        if (jenis.isEmpty() || nomor.isEmpty() || tgl.isEmpty()) {
            Toast.makeText(this, "Lengkapi data wajib!", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CreateDokumenRequest(userId, jenis, nomor, tgl, desk)
        val token = tokenManager.getToken() ?: return

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // Endpoint: POST /api/admin/users/{userId}/dokumen
                val response = RetrofitClient.instance.createDokumenForUser("Bearer $token", userId, request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminAddUserDokumenActivity, "Dokumen berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminAddUserDokumenActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}