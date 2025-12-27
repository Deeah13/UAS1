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
        // PERBAIKAN: Menghapus setSupportActionBar(binding.toolbar)
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSimpan.setOnClickListener { simpanPengajuan() }
    }

    private fun setupSpinner() {
        val options = listOf("REGULER", "FUNGSIONAL", "STRUKTURAL")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJenis.adapter = adapter
    }

    private fun simpanPengajuan() {
        val jenis = binding.spinnerJenis.selectedItem.toString()
        val pangkat = binding.etPangkatTujuan.text.toString().trim()
        val jabatan = binding.etJabatanTujuan.text.toString().trim()

        if (pangkat.isEmpty() || jabatan.isEmpty()) {
            Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
            return
        }

        val request = PengajuanCreateRequestDTO(jenis, pangkat, jabatan)
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSimpan.isEnabled = false

        lifecycleScope.launch {
            try {
                val token = "Bearer ${tokenManager.getToken()}"
                val response = RetrofitClient.instance.createPengajuan(token, request)
                if (response.isSuccessful) {
                    val intent = Intent(this@TambahPengajuanActivity, DetailPengajuanActivity::class.java).apply {
                        putExtra("PENGAJUAN_ID", response.body()?.data?.id)
                    }
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TambahPengajuanActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSimpan.isEnabled = true
            }
        }
    }
}