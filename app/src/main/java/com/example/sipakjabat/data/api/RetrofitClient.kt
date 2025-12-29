package com.example.sipakjabat.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Alamat IP 10.0.2.2 untuk emulator mengakses localhost komputer
    // Pastikan menggunakan IP dari Wireless LAN adapter Wi-Fi
    private const val BASE_URL = "http://192.168.1.3:8080/"

    val instance: ApiService by lazy {
        // Logging agar kita bisa melihat JSON yang dikirim/diterima di Logcat
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        retrofit.create(ApiService::class.java)

    }
}