package com.faiz.bumiloka.adapters

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.BadgeHelper
import com.faiz.bumiloka.BadgeVisualHelper
import com.faiz.bumiloka.R
import com.faiz.bumiloka.model.Badge

class UserBadgeAdapter(private val badgeList: List<Badge>) :
    RecyclerView.Adapter<UserBadgeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivBadgeIcon: ImageView = view.findViewById(R.id.ivBadgeIcon)
        val tvBadgeName: TextView = view.findViewById(R.id.tvBadgeName)
        val cardBadge: View = view.findViewById(R.id.cardBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val badge = badgeList[position]
        holder.tvBadgeName.text = badge.nama
        
        BadgeVisualHelper.renderBadge(holder.ivBadgeIcon, badge.nama, badge.level)
        
        val context = holder.itemView.context
        val isOwned = BadgeHelper.punyaBadge(context, badge.id)
        
        if (isOwned) {
            holder.ivBadgeIcon.colorFilter = null
            holder.ivBadgeIcon.alpha = 1.0f
            holder.tvBadgeName.alpha = 1.0f
        } else {
            // Membuat grayscale untuk lencana yang belum didapat
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            val filter = ColorMatrixColorFilter(matrix)
            holder.ivBadgeIcon.colorFilter = filter
            holder.ivBadgeIcon.alpha = 0.5f
            holder.tvBadgeName.alpha = 0.5f
        }
        
        holder.itemView.setOnClickListener {
            val msg = if (isOwned) badge.deskripsi else "Kriteria: ${badge.kriteria}"
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = badgeList.size
}
