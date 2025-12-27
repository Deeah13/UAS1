package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.PengajuanResponse
import com.example.sipakjabat.databinding.ActivityAdminAllPengajuanBinding
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.launch

class AdminAllPengajuanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAllPengajuanBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var adminAdapter: AdminPengajuanAdapter

    private var fullList: List<PengajuanResponse> = emptyList()
    private var selectedJenis: String = "Semua Jenis"
    private var selectedStatus: String = "Semua Status"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAllPengajuanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        setupUI()
        setupDropdownFilters()
        loadInitialData()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        adminAdapter = AdminPengajuanAdapter(emptyList()) { pengajuan ->
            val intent = Intent(this, AdminDetailVerifikasiActivity::class.java)
            intent.putExtra("PENGAJUAN_ID", pengajuan.id)
            startActivity(intent)
        }

        binding.rvAllPengajuan.apply {
            layoutManager = LinearLayoutManager(this@AdminAllPengajuanActivity)
            adapter = adminAdapter
        }

        // Listener Pencarian Nama/NIP
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { applyCombinedFilters() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupDropdownFilters() {
        // 1. Inisialisasi Filter Jenis
        val jenisList = arrayOf("Semua Jenis", "REGULER", "FUNGSIONAL", "STRUKTURAL")
        val jenisAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, jenisList)
        binding.spinnerFilterJenis.setAdapter(jenisAdapter)
        binding.spinnerFilterJenis.setText(jenisList[0], false)
        binding.spinnerFilterJenis.setOnItemClickListener { _, _, position, _ ->
            selectedJenis = jenisList[position]
            applyCombinedFilters()
        }

        // 2. Inisialisasi Filter Status
        val statusList = arrayOf("Semua Status", "SUBMITTED", "APPROVED", "REJECTED", "PERLU_REVISI")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statusList)
        binding.spinnerFilterStatus.setAdapter(statusAdapter)
        binding.spinnerFilterStatus.setText(statusList[0], false)
        binding.spinnerFilterStatus.setOnItemClickListener { _, _, position, _ ->
            selectedStatus = statusList[position]
            applyCombinedFilters()
        }
    }

    private fun loadInitialData() {
        val token = tokenManager.getToken() ?: return
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // Mengambil seluruh data pengajuan dari endpoint /all
                val response = RetrofitClient.instance.getAllPengajuan("Bearer $token")
                if (response.isSuccessful) {
                    fullList = response.body()?.data ?: emptyList()
                    adminAdapter.updateData(fullList)
                    updateEmptyState(fullList.isEmpty())
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminAllPengajuanActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    /**
     * Logika Filter Gabungan: Nama/NIP + Jenis + Status
     */
    private fun applyCombinedFilters() {
        val query = binding.etSearch.text.toString().lowercase().trim()

        val filteredResult = fullList.filter { pengajuan ->
            // Cek Pencarian
            val matchSearch = pengajuan.user?.namaLengkap?.lowercase()?.contains(query) == true ||
                    pengajuan.user?.nip?.contains(query) == true

            // Cek Jenis
            val matchJenis = selectedJenis == "Semua Jenis" || pengajuan.jenisPengajuan == selectedJenis

            // Cek Status
            val matchStatus = selectedStatus == "Semua Status" || pengajuan.status == selectedStatus

            matchSearch && matchJenis && matchStatus
        }

        adminAdapter.updateData(filteredResult)
        updateEmptyState(filteredResult.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
}