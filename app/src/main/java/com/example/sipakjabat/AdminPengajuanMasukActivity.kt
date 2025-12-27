package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.databinding.ActivityAdminPengajuanMasukBinding
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.launch

class AdminPengajuanMasukActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPengajuanMasukBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var adminAdapter: AdminPengajuanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate binding
        binding = ActivityAdminPengajuanMasukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        setupUI()
        loadData()
    }

    private fun setupUI() {
        // Toolbar setup
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnBack.setOnClickListener { finish() }

        // Setup RecyclerView
        adminAdapter = AdminPengajuanAdapter(emptyList()) { pengajuan ->
            // Navigasi ke detail verifikasi (Akan dibuat di step berikutnya)
            Toast.makeText(this, "Memeriksa: ${pengajuan.jenisPengajuan}", Toast.LENGTH_SHORT).show()
        }

        binding.rvPengajuanMasuk.apply {
            layoutManager = LinearLayoutManager(this@AdminPengajuanMasukActivity)
            adapter = adminAdapter
        }
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.instance.getSubmittedPengajuan("Bearer $token")

                if (response.isSuccessful) {
                    val listData = response.body()?.data ?: emptyList()
                    adminAdapter.updateData(listData)

                    if (listData.isEmpty()) {
                        Toast.makeText(this@AdminPengajuanMasukActivity, "Tidak ada pengajuan masuk", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AdminPengajuanMasukActivity, "Gagal memuat data: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminPengajuanMasukActivity, "Koneksi Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}