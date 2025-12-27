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
import com.example.sipakjabat.databinding.ActivityTambahDokumenBinding
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TambahDokumenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahDokumenBinding
    private lateinit var tokenManager: TokenManager
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahDokumenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        setupUI()
        setupJenisDokumenSpinner()
    }

    private fun setupUI() {
        // Set toolbar as action bar agar tidak error Unresolved Reference
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnBack.setOnClickListener { finish() }
        binding.etTanggal.setOnClickListener { showDatePicker() }
        binding.btnSimpan.setOnClickListener {
            if (validateInput()) {
                simpanDokumen()
            }
        }
    }

    private fun setupJenisDokumenSpinner() {
        val daftarDokumen = listOf("SK_PANGKAT", "SKP", "SK_JABATAN", "PAK", "SK_PELANTIKAN", "SPMT")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, daftarDokumen)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJenis.adapter = adapter
    }

    private fun showDatePicker() {
        DatePickerDialog(this, { _, year, month, day ->
            calendar.set(year, month, day)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            binding.etTanggal.setText(sdf.format(calendar.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.maxDate = System.currentTimeMillis()
            show()
        }
    }

    private fun validateInput(): Boolean {
        if (binding.etNomor.text.toString().trim().isEmpty()) {
            binding.etNomor.error = "Nomor dokumen wajib diisi"
            return false
        }
        if (binding.etTanggal.text.toString().trim().isEmpty()) {
            binding.etTanggal.error = "Tanggal terbit wajib diisi"
            return false
        }
        return true
    }

    private fun simpanDokumen() {
        val request = CreateDokumenRequest(
            userId = null,
            jenisDokumen = binding.spinnerJenis.selectedItem.toString(),
            nomorDokumen = binding.etNomor.text.toString().trim(),
            tanggalTerbit = binding.etTanggal.text.toString().trim(),
            deskripsi = binding.etDeskripsi.text.toString().trim()
        )

        setLoadingState(true)
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token != null) {
                    val response = RetrofitClient.instance.createDocument("Bearer $token", request)
                    if (response.isSuccessful) {
                        Toast.makeText(this@TambahDokumenActivity, "Dokumen berhasil disimpan", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Gagal menyimpan"
                        Toast.makeText(this@TambahDokumenActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@TambahDokumenActivity, "Terjadi kesalahan koneksi", Toast.LENGTH_LONG).show()
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSimpan.isEnabled = !isLoading
        binding.btnSimpan.text = if (isLoading) "Proses..." else "SIMPAN DOKUMEN"
    }
}