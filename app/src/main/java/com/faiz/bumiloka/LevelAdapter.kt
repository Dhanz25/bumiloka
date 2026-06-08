package com.faiz.bumiloka

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class LevelAdapter(
    private val list: List<LevelModel>,
    private val onItemClick: (LevelModel) -> Unit
) : RecyclerView.Adapter<LevelAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardLevel)
        val ivIcon: ImageView = view.findViewById(R.id.ivLevelIcon)
        val tvNumber: TextView = view.findViewById(R.id.tvLevelNumber)
        val tvTitle: TextView = view.findViewById(R.id.tvLevelTitle)
        val ivLock: ImageView = view.findViewById(R.id.ivLockStatus)
        val tvActive: TextView = view.findViewById(R.id.tvActiveBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_level, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNumber.text = "Level ${item.level}"
        holder.tvTitle.text = item.title

        if (item.isUnlocked) {
            holder.ivLock.visibility = View.GONE
            holder.card.alpha = 1.0f
            holder.card.isEnabled = true
            
            if (item.isActive) {
                holder.tvActive.visibility = View.VISIBLE
                holder.card.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.nav_active)
                holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.home_background))
            } else {
                holder.tvActive.visibility = View.GONE
                holder.card.strokeColor = ContextCompat.getColor(holder.itemView.context, android.R.color.transparent)
                holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            }
        } else {
            holder.ivLock.visibility = View.VISIBLE
            holder.ivLock.setImageResource(R.drawable.lock)
            holder.card.alpha = 0.6f
            holder.card.strokeColor = ContextCompat.getColor(holder.itemView.context, android.R.color.transparent)
            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray))
            holder.tvActive.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = list.size
}
