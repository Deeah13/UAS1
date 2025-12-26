package com.example.sipakjabat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sipakjabat.data.model.DokumenResponse

class SelectableDokumenAdapter(
    private val documents: List<DokumenResponse>,
    private val selectedIds: Set<Long>,
    private val onDocumentToggle: (Long) -> Unit
) : RecyclerView.Adapter<SelectableDokumenAdapter.ViewHolder>() {

    private val currentSelection = selectedIds.toMutableSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selectable_document, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(documents[position], currentSelection.contains(documents[position].id))
    }

    override fun getItemCount() = documents.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
        private val tvJenisDokumen: TextView = itemView.findViewById(R.id.tvJenisDokumen)
        private val tvNomorDokumen: TextView = itemView.findViewById(R.id.tvNomorDokumen)

        fun bind(dokumen: DokumenResponse, isSelected: Boolean) {
            checkbox.isChecked = isSelected
            tvJenisDokumen.text = formatJenisDokumen(dokumen.jenisDokumen)
            tvNomorDokumen.text = dokumen.nomorDokumen

            itemView.setOnClickListener {
                onDocumentToggle(dokumen.id)
                if (currentSelection.contains(dokumen.id)) {
                    currentSelection.remove(dokumen.id)
                } else {
                    currentSelection.add(dokumen.id)
                }
                notifyItemChanged(adapterPosition)
            }

            checkbox.setOnClickListener {
                onDocumentToggle(dokumen.id)
                if (currentSelection.contains(dokumen.id)) {
                    currentSelection.remove(dokumen.id)
                } else {
                    currentSelection.add(dokumen.id)
                }
            }
        }

        private fun formatJenisDokumen(jenis: String): String {
            return when (jenis) {
                "SK_PANGKAT" -> "SK Pangkat"
                "SKP" -> "SKP"
                "SK_JABATAN" -> "SK Jabatan"
                "PAK" -> "PAK"
                "SK_PELANTIKAN" -> "SK Pelantikan"
                "SPMT" -> "SPMT"
                else -> jenis
            }
        }
    }
}