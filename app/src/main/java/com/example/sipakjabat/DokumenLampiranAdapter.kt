package com.example.sipakjabat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sipakjabat.data.model.DokumenResponse

class DokumenLampiranAdapter : ListAdapter<DokumenResponse, DokumenLampiranAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dokumen_lampiran, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJenisDokumen: TextView = itemView.findViewById(R.id.tvJenisDokumen)
        private val tvNomorDokumen: TextView = itemView.findViewById(R.id.tvNomorDokumen)
        private val tvTanggalTerbit: TextView = itemView.findViewById(R.id.tvTanggalTerbit)
        private val tvDeskripsi: TextView = itemView.findViewById(R.id.tvDeskripsi)

        fun bind(dokumen: DokumenResponse) {
            tvJenisDokumen.text = formatJenisDokumen(dokumen.jenisDokumen)
            tvNomorDokumen.text = dokumen.nomorDokumen
            tvTanggalTerbit.text = dokumen.tanggalTerbit
            tvDeskripsi.text = dokumen.deskripsi ?: "-"
        }

        private fun formatJenisDokumen(jenis: String): String {
            return when (jenis) {
                "SK_PANGKAT" -> "SK Pangkat"
                "SKP" -> "SKP (2 Tahun Terakhir)"
                "SK_JABATAN" -> "SK Jabatan"
                "PAK" -> "Penilaian Angka Kredit"
                "SK_PELANTIKAN" -> "SK Pelantikan"
                "SPMT" -> "Surat Perintah Melaksanakan Tugas"
                else -> jenis
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DokumenResponse>() {
        override fun areItemsTheSame(oldItem: DokumenResponse, newItem: DokumenResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DokumenResponse, newItem: DokumenResponse): Boolean {
            return oldItem == newItem
        }
    }
}