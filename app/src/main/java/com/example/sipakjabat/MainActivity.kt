package com.example.sipakjabat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sipakjabat.data.api.RetrofitClient
import com.example.sipakjabat.data.model.LoginRequest
import com.example.sipakjabat.databinding.ActivityMainBinding
import com.example.sipakjabat.utils.TokenManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        // Auto-login check
        if (tokenManager.getToken() != null && tokenManager.getRole() != null) {
            navigateToDashboard(tokenManager.getRole()!!)
            return // Important to prevent further execution
        }

        binding.btnLogin.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val nip = binding.etNip.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (nip.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "NIP dan Password wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        setLoadingState(true)

        lifecycleScope.launch {
            try {
                val loginRequest = LoginRequest(nip, password)
                val loginResponse = RetrofitClient.instance.login(loginRequest)

                if (loginResponse.isSuccessful) {
                    val token = loginResponse.body()?.token
                    if (token != null) {
                        fetchProfileAndRedirect("Bearer $token")
                    } else {
                        handleLoginError("Token tidak diterima dari server.")
                    }
                } else {
                    handleLoginError("NIP atau Password salah.")
                }
            } catch (e: Exception) {
                handleLoginError("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    private suspend fun fetchProfileAndRedirect(tokenWithBearer: String) {
        try {
            val profileResponse = RetrofitClient.instance.getProfile(tokenWithBearer)

            if (profileResponse.isSuccessful) {
                val profile = profileResponse.body()?.data
                val role = profile?.role
                val pureToken = tokenWithBearer.removePrefix("Bearer ")

                if (role != null) {
                    tokenManager.saveAuthData(pureToken, role)
                    navigateToDashboard(role)
                } else {
                    handleLoginError("Gagal mendapatkan peran pengguna.")
                }
            } else {
                handleLoginError("Gagal mengambil data profil.")
            }
        } catch (e: Exception) {
            handleLoginError("Kesalahan jaringan saat mengambil profil: ${e.message}")
        }
    }

    private fun navigateToDashboard(role: String) {
        val intent = if (role.equals("VERIFIKATOR", ignoreCase = true)) {
            // Asumsi AdminActivity ada untuk verifikator
            Intent(this, AdminActivity::class.java)
        } else {
            Intent(this, PegawaiActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun handleLoginError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        setLoadingState(false)
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.text = if (isLoading) "" else "MASUK"
        binding.btnLogin.isEnabled = !isLoading
    }
}