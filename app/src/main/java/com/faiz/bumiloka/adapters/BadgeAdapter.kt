package com.faiz.bumiloka.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.BadgeVisualHelper
import com.faiz.bumiloka.R
import com.faiz.bumiloka.model.Badge

class BadgeAdapter(
    private val list: MutableList<Badge>,
    private val onEdit: (Badge) -> Unit,
    private val onDelete: (Badge) -> Unit
) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    class BadgeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.iv_badge_icon)
        val tvNama: TextView = view.findViewById(R.id.tv_badge_name)
        val tvLevel: TextView = view.findViewById(R.id.tv_badge_level)
        val tvDesc: TextView = view.findViewById(R.id.tv_badge_desc)
        val btnEdit: ImageButton = view.findViewById(R.id.btn_edit_badge)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_badge_admin, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = list[position]
        holder.tvNama.text = badge.nama
        holder.tvLevel.text = if (badge.level == 0) "Umum" else "Level ${badge.level}"
        holder.tvDesc.text = badge.deskripsi

        // SINKRONISASI: Gambar lencana sesuai Nama DAN Level
        BadgeVisualHelper.renderBadge(holder.ivIcon, badge.nama, badge.level)

        holder.btnEdit.setOnClickListener { onEdit(badge) }
        holder.btnDelete.setOnClickListener { onDelete(badge) }
    }

    override fun getItemCount(): Int = list.size
}
