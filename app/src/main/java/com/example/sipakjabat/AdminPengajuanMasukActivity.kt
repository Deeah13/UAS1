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
        binding = ActivityAdminPengajuanMasukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        // Mengambil data setiap kali activity aktif agar daftar selalu terbaru
        loadData()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        adminAdapter = AdminPengajuanAdapter(emptyList()) { pengajuan ->
            val intent = Intent(this, AdminDetailVerifikasiActivity::class.java).apply {
                putExtra("PENGAJUAN_ID", pengajuan.id)
                // Identitas Pegawai
                putExtra("NAMA", pengajuan.user?.namaLengkap)
                putExtra("NIP", pengajuan.user?.nip)
                putExtra("EMAIL", pengajuan.user?.email)
                // Rincian Pengajuan
                putExtra("JENIS", pengajuan.jenisPengajuan)
                putExtra("PANGKAT_SAAT_INI", pengajuan.pangkatSaatIni)
                putExtra("JABATAN_SAAT_INI", pengajuan.jabatanSaatIni)
                putExtra("PANGKAT_TUJUAN", pengajuan.pangkatTujuan)
                putExtra("JABATAN_TUJUAN", pengajuan.jabatanTujuan)
            }
            startActivity(intent)
        }

        binding.rvPengajuanMasuk.apply {
            layoutManager = LinearLayoutManager(this@AdminPengajuanMasukActivity)
            adapter = adminAdapter
        }
    }

    private fun loadData() {
        val token = tokenManager.getToken()
        if (token == null) {
            Toast.makeText(this, "Sesi habis, silakan login kembali", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // Mengambil pengajuan yang berstatus SUBMITTED dari backend
                val response = RetrofitClient.instance.getSubmittedPengajuan("Bearer $token")

                if (response.isSuccessful) {
                    val listData = response.body()?.data ?: emptyList()
                    adminAdapter.updateData(listData)

                    if (listData.isEmpty()) {
                        Toast.makeText(this@AdminPengajuanMasukActivity, "Tidak ada pengajuan baru", Toast.LENGTH_SHORT).show()
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