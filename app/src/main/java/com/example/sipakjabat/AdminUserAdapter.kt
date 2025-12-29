package com.example.sipakjabat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sipakjabat.data.model.UserResponse
import com.example.sipakjabat.databinding.ItemUserBinding

class AdminUserAdapter(private val onClick: (UserResponse) -> Unit) :
    ListAdapter<UserResponse, AdminUserAdapter.UserViewHolder>(DiffCallback) {

    class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserResponse, onClick: (UserResponse) -> Unit) {
            // 1. Set Data Tekstual
            binding.tvNamaUser.text = user.namaLengkap ?: "Tanpa Nama"
            binding.tvNipUser.text = "NIP: ${user.nip ?: "-"}"

            // 2. Logika Warna Role
            val (cardBg, accentColor, roleLabel) = when (user.role) {
                "VERIFIKATOR" -> Triple("#FFF9E6", "#C5A059", "VERIFIKATOR") // Gold Style
                "ADMIN" -> Triple("#E8F5E9", "#2E7D32", "ADMINISTRATOR")     // Green Style
                else -> Triple("#E6F2FF", "#014488", "PEGAWAI")             // Blue Style
            }

            // Terapkan Warna ke Seluruh Kotak (CardView)
            binding.rootCard.setCardBackgroundColor(Color.parseColor(cardBg))
            binding.rootCard.strokeColor = Color.parseColor(accentColor)

            // Terapkan ke Badge Role
            binding.tvRoleUser.text = roleLabel
            binding.tvRoleUser.setBackgroundColor(Color.parseColor(accentColor))
            binding.tvRoleUser.setTextColor(Color.WHITE)

            // 3. Listener Klik
            binding.root.setOnClickListener { onClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<UserResponse>() {
        override fun areItemsTheSame(oldItem: UserResponse, newItem: UserResponse) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: UserResponse, newItem: UserResponse) =
            oldItem == newItem
    }
}