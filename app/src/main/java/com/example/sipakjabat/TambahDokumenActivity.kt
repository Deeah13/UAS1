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

    // Variabel untuk menyimpan ID user yang sedang login
    private var userId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahDokumenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        // AMBIL USER_ID DARI INTENT (Pastikan Dashboard mengirim putExtra("USER_ID", id))
        userId = intent.getLongExtra("USER_ID", -1)

        if (userId == -1L) {
            Toast.makeText(this, "ID Pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupJenisDokumenSpinner()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnBack.setOnClickListener { finish() }
        binding.etTanggal.setOnClickListener { showDatePicker() }
        binding.btnSimpan.setOnClickListener { simpanDokumen() }
    }

    private fun setupJenisDokumenSpinner() {
        val daftarDokumen = listOf("SK_PANGKAT", "SKP", "SK_JABATAN", "PAK", "SK_PELANTIKAN", "SPMT")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, daftarDokumen)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJenis.adapter = adapter
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                binding.etTanggal.setText(sdf.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun simpanDokumen() {
        val jenis = binding.spinnerJenis.selectedItem.toString()
        val nomor = binding.etNomor.text.toString().trim()
        val tanggal = binding.etTanggal.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()

        if (nomor.isEmpty() || tanggal.isEmpty()) {
            Toast.makeText(this, "Nomor dan Tanggal Terbit wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        // FIX: Tambahkan userId dan jangan kirim null pada deskripsi agar tidak Mismatch
        val request = CreateDokumenRequest(
            userId = userId,               // Menyelesaikan error 'No value passed for parameter userId'
            jenisDokumen = jenis,
            nomorDokumen = nomor,
            tanggalTerbit = tanggal,
            deskripsi = deskripsi          // Menghapus .ifEmpty { null } agar tetap String (bukan String?)
        )

        setLoadingState(true)

        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token != null) {
                    // Endpoint untuk Pegawai: POST api/pegawai/dokumen
                    val response = RetrofitClient.instance.createDocument("Bearer $token", request)

                    if (response.isSuccessful) {
                        Toast.makeText(this@TambahDokumenActivity, "Dokumen berhasil disimpan", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@TambahDokumenActivity, "Gagal menyimpan dokumen", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@TambahDokumenActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSimpan.isEnabled = !isLoading
        binding.btnSimpan.text = if (isLoading) "Menyimpan..." else "SIMPAN DOKUMEN"
    }
}