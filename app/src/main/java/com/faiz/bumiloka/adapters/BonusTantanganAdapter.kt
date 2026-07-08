package com.faiz.bumiloka.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.R
import com.faiz.bumiloka.model.BonusChallengeModel

class BonusTantanganAdapter(
    private val list: List<BonusChallengeModel>,
    private val onEdit: (BonusChallengeModel) -> Unit,
    private val onDelete: (BonusChallengeModel) -> Unit
) : RecyclerView.Adapter<BonusTantanganAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJudul: TextView = view.findViewById(R.id.tv_judul)
        val tvDeskripsi: TextView = view.findViewById(R.id.tv_deskripsi)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val btnEdit: ImageButton = view.findViewById(R.id.btn_edit)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bonus_tantangan_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvJudul.text = item.judul
        holder.tvDeskripsi.text = item.deskripsi
        holder.tvStatus.text = if (item.aktif) "Aktif" else "Nonaktif"
        holder.tvStatus.setTextColor(if (item.aktif) 0xFF4CAF50.toInt() else 0xFFE53935.toInt())

        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = list.size
}
