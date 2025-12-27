package com.example.sipakjabat

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
            binding.tvNamaUser.text = user.namaLengkap
            binding.tvNipUser.text = "NIP: ${user.nip}"
            binding.tvRoleUser.text = user.role

            // Logika warna role sesuai sistem backend
            if (user.role == "VERIFIKATOR") {
                binding.tvRoleUser.setTextColor(0xFF014488.toInt())
                binding.tvRoleUser.setBackgroundColor(0xFFEBF8FF.toInt())
            } else {
                binding.tvRoleUser.setTextColor(0xFF4A5568.toInt())
                binding.tvRoleUser.setBackgroundColor(0xFFEDF2F7.toInt())
            }

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
        override fun areItemsTheSame(oldItem: UserResponse, newItem: UserResponse) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: UserResponse, newItem: UserResponse) = oldItem == newItem
    }
}