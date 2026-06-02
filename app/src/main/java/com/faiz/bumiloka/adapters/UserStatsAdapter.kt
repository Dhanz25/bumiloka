package com.faiz.bumiloka.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.model.UserModel
import com.faiz.bumiloka.R

class UserStatsAdapter(
    private val list: MutableList<UserModel>
) : RecyclerView.Adapter<UserStatsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tv_rank_user)
        val tvNama: TextView = view.findViewById(R.id.tv_nama_user)
        val tvEmail: TextView = view.findViewById(R.id.tv_email_user)
        val tvPoin: TextView = view.findViewById(R.id.tv_total_poin)
        val tvKuis: TextView = view.findViewById(R.id.tv_kuis_selesai)
        val tvEdukasi: TextView = view.findViewById(R.id.tv_edukasi_dibaca)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_stats, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = list[position]
        holder.tvRank.text = "#${position + 1}"
        holder.tvNama.text = user.nama
        holder.tvEmail.text = user.email
        holder.tvPoin.text = "${user.totalPoin} poin"
        holder.tvKuis.text = "Kuis: ${user.kuisSelesai}"
        holder.tvEdukasi.text = "Materi: ${user.edukasiDibaca}"
    }

    override fun getItemCount() = list.size
}