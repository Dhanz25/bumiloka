package com.faiz.bumiloka.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.model.Edukasi
import com.faiz.bumiloka.R

class MateriAdapter(
    private val list: MutableList<Edukasi>,
    private val onEdit: (Edukasi) -> Unit,
    private val onDelete: (Edukasi) -> Unit
) : RecyclerView.Adapter<MateriAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJudul: TextView = view.findViewById(R.id.tv_judul_edukasi)
        val tvKategori: TextView = view.findViewById(R.id.tv_kategori_edukasi)
        val tvPreview: TextView = view.findViewById(R.id.tv_konten_preview)
        val btnEdit: ImageButton = view.findViewById(R.id.btn_edit_edukasi)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_edukasi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_edukasi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val materi = list[position]
        holder.tvJudul.text = materi.title
        holder.tvKategori.text = materi.description
        holder.tvPreview.text = if (materi.content.length > 80)
            materi.content.substring(0, 80) + "..." else materi.content
        holder.btnEdit.setOnClickListener { onEdit(materi) }
        holder.btnDelete.setOnClickListener { onDelete(materi) }
    }

    override fun getItemCount() = list.size
}