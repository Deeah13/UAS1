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
        // Upgrade Toolbar & Navigation
        setSupportActionBar(binding.toolbar)
        // Menonaktifkan judul default agar tidak tumpang tindih dengan TextView kustom
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Klik tombol back manual sesuai desain header tinggi baru
        binding.btnBack.setOnClickListener { finish() }

        // Listener untuk elemen input
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

        val request = CreateDokumenRequest(
            jenisDokumen = jenis,
            nomorDokumen = nomor,
            tanggalTerbit = tanggal,
            deskripsi = deskripsi.ifEmpty { null }
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
                        val errorMsg = response.body()?.message ?: "Gagal menyimpan: ${response.code()}"
                        Toast.makeText(this@TambahDokumenActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@TambahDokumenActivity, "Sesi berakhir", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TambahDokumenActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        // Kontrol visibilitas progressBar melalui binding
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSimpan.isEnabled = !isLoading
        binding.btnSimpan.text = if (isLoading) "Menyimpan..." else "SIMPAN DOKUMEN"
    }
}