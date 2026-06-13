package com.faiz.bumiloka.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.model.Tantangan
import com.faiz.bumiloka.R

class TantanganAdapter(
    private val list: MutableList<Tantangan>,
    private val onEdit: (Tantangan) -> Unit,
    private val onDelete: (Tantangan) -> Unit,
    private val onToggleAktif: (Tantangan) -> Unit
) : RecyclerView.Adapter<TantanganAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJudul: TextView = view.findViewById(R.id.tv_judul_tantangan)
        val tvDeskripsi: TextView = view.findViewById(R.id.tv_deskripsi_tantangan)
        val tvStatus: TextView = view.findViewById(R.id.tv_status_tantangan)
        val swAktif: Switch = view.findViewById(R.id.switch_aktif)
        val btnEdit: ImageButton = view.findViewById(R.id.btn_edit_tantangan)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_tantangan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tantangan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tantangan = list[position]
        holder.tvJudul.text = tantangan.judul
        holder.tvDeskripsi.text = tantangan.deskripsi
        
        holder.tvStatus.text = if (tantangan.aktif) "● Aktif" else "● Nonaktif"
        holder.tvStatus.setTextColor(
            if (tantangan.aktif) android.graphics.Color.parseColor("#4CAF50")
            else android.graphics.Color.parseColor("#F44336")
        )
        
        // Reset listener dulu supaya tidak trigger saat bind
        holder.swAktif.setOnCheckedChangeListener(null)
        holder.swAktif.isChecked = tantangan.aktif
        holder.swAktif.setOnCheckedChangeListener { _, _ -> onToggleAktif(tantangan) }

        holder.btnEdit.setOnClickListener { onEdit(tantangan) }
        holder.btnDelete.setOnClickListener { onDelete(tantangan) }
    }

    override fun getItemCount() = list.size
}