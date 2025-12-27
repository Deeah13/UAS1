// app/src/main/java/com/example/sipakjabat/AdminPengajuanAdapter.kt
package com.example.sipakjabat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sipakjabat.data.model.PengajuanResponse
import com.example.sipakjabat.databinding.ItemPengajuanBinding

class AdminPengajuanAdapter(
    private var list: List<PengajuanResponse>,
    private val onItemClick: (PengajuanResponse) -> Unit
) : RecyclerView.Adapter<AdminPengajuanAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemPengajuanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PengajuanResponse) {
            // Menampilkan Identitas: Nama (NIP)
            val nama = item.user?.namaLengkap ?: "Pegawai"
            val nip = item.user?.nip ?: "-"
            binding.tvJenis.text = "$nama ($nip)"

            binding.tvTanggal.text = "Jenis: ${item.jenisPengajuan} | ${item.tanggalDibuat}"
            binding.tvStatus.text = item.status

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPengajuanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<PengajuanResponse>) {
        list = newList
        notifyDataSetChanged()
    }
}