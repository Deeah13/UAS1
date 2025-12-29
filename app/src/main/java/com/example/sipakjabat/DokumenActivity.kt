package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.DokumenResponse
import com.example.sipakjabat.databinding.ActivityDokumenBinding
import com.example.sipakjabat.utils.TokenManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class DokumenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDokumenBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var dokumenAdapter: DokumenAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDokumenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupUI()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadDokumen()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnBack.setOnClickListener { finish() }

        binding.fabAddDokumen.setOnClickListener {
            startActivity(Intent(this, TambahDokumenActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        dokumenAdapter = DokumenAdapter(emptyList(), { dokumen ->
            showDeleteConfirmationDialog(dokumen)
        })
        binding.rvDokumen.apply {
            layoutManager = LinearLayoutManager(this@DokumenActivity)
            adapter = dokumenAdapter
        }
    }

    private fun loadDokumen() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    Toast.makeText(this@DokumenActivity, "Silakan login kembali", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val response = RetrofitClient.instance.getMyDocuments("Bearer $token")
                if (response.isSuccessful) {
                    val dokumenList = response.body()?.data ?: emptyList()
                    dokumenAdapter.updateData(dokumenList)

                    // Set teks kembali ke status default saat tidak ada data
                    binding.tvEmpty.text = "Belum ada dokumen"
                    binding.tvEmpty.visibility = if (dokumenList.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    handleApiError("Gagal memuat dokumen")
                }
            } catch (e: Exception) {
                handleApiError("Error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showDeleteConfirmationDialog(dokumen: DokumenResponse) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Dokumen")
            .setMessage("Apakah Anda yakin ingin menghapus '${dokumen.jenisDokumen}'?")
            .setPositiveButton("Hapus") { _, _ -> deleteDokumen(dokumen.id) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteDokumen(id: Long) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.instance.deleteDocument("Bearer $token", id)

                if (response.isSuccessful) {
                    Toast.makeText(this@DokumenActivity, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                    loadDokumen()
                } else {
                    handleApiError("Gagal menghapus dokumen")
                }
            } catch (e: Exception) {
                handleApiError("Kesalahan sistem: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvDokumen.visibility = if (isLoading) View.GONE else View.VISIBLE
        // Sembunyikan teks background saat sedang loading agar tidak tumpang tindih
        if (isLoading) binding.tvEmpty.visibility = View.GONE
    }

    private fun handleApiError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}