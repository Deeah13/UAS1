package com.example.sipakjabat

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sipakjabat.data.model.PengajuanResponse
import com.example.sipakjabat.databinding.ItemPengajuanBinding

class PengajuanAdapter(private var pengajuanList: List<PengajuanResponse>) :
    RecyclerView.Adapter<PengajuanAdapter.ViewHolder>() {

    fun updateData(newPengajuanList: List<PengajuanResponse>) {
        this.pengajuanList = newPengajuanList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemPengajuanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPengajuanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = pengajuanList[position]

        holder.binding.tvJenis.text = item.jenisPengajuan
        holder.binding.tvStatus.text = item.status
        holder.binding.tvTanggal.text = item.tanggalDibuat ?: "-"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailPengajuanActivity::class.java).apply {
                putExtra("PENGAJUAN_ID", item.id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = pengajuanList.size
}