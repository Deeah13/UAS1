package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.UserResponse
import com.example.sipakjabat.databinding.ActivityAdminUserListBinding
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.launch

class AdminUserListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminUserListBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var userAdapter: AdminUserAdapter
    private var fullUserList: List<UserResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
    }

    private fun setupRecyclerView() {
        // PERBAIKAN: Menentukan tipe data secara eksplisit (user: UserResponse)
        userAdapter = AdminUserAdapter { user: UserResponse ->
            val intent = Intent(this@AdminUserListActivity, AdminUserDetailActivity::class.java)
            intent.putExtra("USER_ID", user.id)
            startActivity(intent)
        }

        binding.rvUserList.apply {
            layoutManager = LinearLayoutManager(this@AdminUserListActivity)
            adapter = userAdapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.fabAddUser.setOnClickListener {
            // Berpindah ke halaman tambah user sesuai endpoint POST /api/admin/users
            val intent = Intent(this@AdminUserListActivity, AdminAddUserActivity::class.java)
            startActivity(intent)
        }

        binding.etSearchUser.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterUsers(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadUsers() {
        val token = tokenManager.getToken() ?: return
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getAllUsers("Bearer $token")
                if (response.isSuccessful) {
                    fullUserList = response.body()?.data ?: emptyList()
                    userAdapter.submitList(fullUserList)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminUserListActivity, "Koneksi Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun filterUsers(query: String) {
        val filtered = fullUserList.filter {
            it.namaLengkap.contains(query, ignoreCase = true) || it.nip.contains(query)
        }
        userAdapter.submitList(filtered)
    }
}