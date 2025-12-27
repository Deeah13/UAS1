package com.example.sipakjabat

import android.content.res.ColorStateList
import android.graphics.Color
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
            // 1. Menampilkan Identitas Utama: Nama (NIP)
            val nama = item.user?.namaLengkap ?: "Pegawai"
            val nip = item.user?.nip ?: "-"
            binding.tvNamaPegawai.text = "$nama ($nip)"

            // 2. Menampilkan Detail Pengajuan
            binding.tvJenis.text = "Jenis: ${item.jenisPengajuan}"
            binding.tvTanggal.text = item.tanggalDibuat ?: "-"

            // 3. Logika Warna Keseluruhan Kotak & Badge (Sesuai Instruksi Tuan)
            // Triple: (Warna Latar Kotak Pastel, Warna Badge/Stroke Pekat, Teks Label)
            val (cardBg, mainColor, statusText) = when (item.status) {
                "APPROVED" -> Triple("#E8F5E9", "#2E7D32", "SETUJU")    // Hijau Pastel & Hijau Tua
                "REJECTED" -> Triple("#FFEBEE", "#DC2626", "DITOLAK")  // Merah Pastel & Merah Tua
                "PERLU_REVISI" -> Triple("#FFF9E6", "#C5A059", "REVISI") // Krem & Emas
                "SUBMITTED" -> Triple("#E6F2FF", "#014488", "DIAJUKAN") // Biru Pastel & Navy
                else -> Triple("#F7FAFC", "#718096", item.status ?: "DRAF")
            }

            // Terapkan ke Seluruh Kotak (CardView)
            binding.rootCard.setCardBackgroundColor(Color.parseColor(cardBg))
            binding.rootCard.strokeColor = Color.parseColor(mainColor)

            // Terapkan ke Badge Status
            binding.tvStatus.text = statusText
            binding.tvStatus.setBackgroundColor(Color.parseColor(mainColor))

            // Terapkan ke Chevron (Ikon Panah) agar warnanya senada
            binding.ivChevron.imageTintList = ColorStateList.valueOf(Color.parseColor(mainColor))

            // 4. Klik Listener
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPengajuanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
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