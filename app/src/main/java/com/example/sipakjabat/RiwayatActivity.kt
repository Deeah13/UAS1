package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.databinding.ActivityRiwayatBinding
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.launch

class RiwayatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRiwayatBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var pengajuanAdapter: PengajuanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        setupUI()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupUI() {
        // PERBAIKAN: Menghapus setSupportActionBar(binding.toolbar) karena Toolbar sudah diganti View
        binding.btnBack.setOnClickListener { finish() }

        binding.fabAddPengajuan.setOnClickListener {
            startActivity(Intent(this, TambahPengajuanActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        pengajuanAdapter = PengajuanAdapter(emptyList())
        binding.rvPengajuan.apply {
            layoutManager = LinearLayoutManager(this@RiwayatActivity)
            adapter = pengajuanAdapter
        }
    }

    private fun loadData() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.instance.getRiwayatPengajuan("Bearer $token")
                if (response.isSuccessful) {
                    val dataList = response.body()?.data ?: emptyList()
                    pengajuanAdapter.updateData(dataList)
                    binding.tvEmpty.visibility = if (dataList.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@RiwayatActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvPengajuan.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
}