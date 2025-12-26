package com.example.sipakjabat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sipakjabat.utils.TokenManager

class PengajuanViewModelFactory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PengajuanDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PengajuanDetailViewModel(tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}