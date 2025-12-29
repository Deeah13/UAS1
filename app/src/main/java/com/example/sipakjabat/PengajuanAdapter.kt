package com.example.sipakjabat

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
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
        val status = item.status.uppercase()

        // Mapping Data
        holder.binding.tvNamaPegawai.text = item.user?.namaLengkap ?: "Nama Tidak Tersedia"
        holder.binding.tvJenis.text = item.jenisPengajuan
        holder.binding.tvStatus.text = item.status
        holder.binding.tvTanggal.text = item.tanggalDibuat

        when (status) {
            "APPROVED" -> { // Diterima
                holder.binding.rootCard.setCardBackgroundColor(Color.parseColor("#E8F5E9")) // Pastel Hijau
                holder.binding.rootCard.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#2E7D32")))
                holder.binding.cardStatusBadge.setCardBackgroundColor(Color.parseColor("#2E7D32"))
            }
            "REJECTED" -> { // Ditolak
                holder.binding.rootCard.setCardBackgroundColor(Color.parseColor("#FFEBEE")) // Pastel Merah
                holder.binding.rootCard.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#C62828")))
                holder.binding.cardStatusBadge.setCardBackgroundColor(Color.parseColor("#C62828"))
            }
            "SUBMITTED", "PERLU_REVISI" -> { // Proses
                holder.binding.rootCard.setCardBackgroundColor(Color.parseColor("#FFF8E1")) // Pastel Kuning/Amber
                holder.binding.rootCard.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#F57F17")))
                holder.binding.cardStatusBadge.setCardBackgroundColor(Color.parseColor("#F57F17"))
            }
            else -> { // Draft atau status lainnya
                holder.binding.rootCard.setCardBackgroundColor(Color.parseColor("#F5F5F5")) // Pastel Abu
                holder.binding.rootCard.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#9E9E9E")))
                holder.binding.cardStatusBadge.setCardBackgroundColor(Color.parseColor("#9E9E9E"))
            }
        }

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