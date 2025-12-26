package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.PengajuanCreateRequestDTO
import com.example.sipakjabat.databinding.ActivityTambahPengajuanBinding
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.launch

class TambahPengajuanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahPengajuanBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahPengajuanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupUI()
        setupSpinner()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Buat Draf Pengajuan"
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnSimpan.setOnClickListener {
            simpanPengajuan()
        }
    }

    private fun setupSpinner() {
        // Corresponds to the ENUM on the backend
        val jenisPengajuanList = listOf("KENAIKAN_PANGKAT", "KENAIKAN_JABATAN_FUNGSIONAL")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, jenisPengajuanList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJenis.adapter = adapter
    }

    private fun simpanPengajuan() {
        val jenisPengajuan = binding.spinnerJenis.selectedItem.toString()
        val pangkatTujuan = binding.etPangkatTujuan.text.toString().trim()
        val jabatanTujuan = binding.etJabatanTujuan.text.toString().trim()

        if (pangkatTujuan.isEmpty() || jabatanTujuan.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val request = PengajuanCreateRequestDTO(
            jenisPengajuan = jenisPengajuan,
            pangkatTujuan = pangkatTujuan,
            jabatanTujuan = jabatanTujuan
        )

        setLoadingState(true)

        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    Toast.makeText(this@TambahPengajuanActivity, "Sesi berakhir, silakan login kembali", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val response = RetrofitClient.instance.createPengajuan("Bearer $token", request)

                if (response.isSuccessful) {
                    Toast.makeText(this@TambahPengajuanActivity, "Draf pengajuan berhasil dibuat", Toast.LENGTH_SHORT).show()
                    val newPengajuan = response.body()?.data
                    if (newPengajuan != null) {
                        // Redirect to detail page to manage attachments
                        val intent = Intent(this@TambahPengajuanActivity, DetailPengajuanActivity::class.java).apply {
                            putExtra("PENGAJUAN_ID", newPengajuan.id)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        handleApiError("Gagal mendapatkan data draf baru.")
                    }
                } else {
                    handleApiError("Gagal membuat draf: ${response.message()}")
                }
            } catch (e: Exception) {
                handleApiError("Terjadi kesalahan: ${e.message}")
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSimpan.isEnabled = !isLoading
        binding.btnSimpan.text = if (isLoading) "Menyimpan..." else "SIMPAN & LANJUT"
    }

    private fun handleApiError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}