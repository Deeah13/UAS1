package com.example.sipakjabat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sipakjabat.data.model.DokumenResponse
import com.example.sipakjabat.databinding.ItemDokumenBinding

class DokumenAdapter(
    private var documents: List<DokumenResponse>,
    private val onDeleteClick: (DokumenResponse) -> Unit,
    private val showDeleteButton: Boolean = true
) : RecyclerView.Adapter<DokumenAdapter.ViewHolder>() {

    fun updateData(newDocuments: List<DokumenResponse>) {
        this.documents = newDocuments
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemDokumenBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(document: DokumenResponse, showDelete: Boolean, clickListener: (DokumenResponse) -> Unit) {
            binding.tvJenisDokumen.text = document.jenisDokumen
            binding.tvNomorDokumen.text = "No: ${document.nomorDokumen}"
            binding.tvTanggalTerbit.text = "Terbit: ${document.tanggalTerbit}"

            // Logika Menampilkan Deskripsi
            if (!document.deskripsi.isNullOrBlank()) {
                binding.tvDeskripsi.visibility = View.VISIBLE
                binding.tvDeskripsi.text = document.deskripsi
            } else {
                binding.tvDeskripsi.visibility = View.GONE
            }

            binding.btnDelete.visibility = if (showDelete) View.VISIBLE else View.GONE
            binding.btnDelete.setOnClickListener { clickListener(document) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDokumenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(documents[position], showDeleteButton, onDeleteClick)
    }

    override fun getItemCount(): Int = documents.size
}