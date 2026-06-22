package com.faiz.bumiloka.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.BadgeHelper
import com.faiz.bumiloka.R
import com.faiz.bumiloka.TantanganStatusHelper
import com.faiz.bumiloka.model.Tantangan

class TantanganBonusAdapter(
    private val list: List<Tantangan>,
    private val onMulai: (Tantangan) -> Unit
) : RecyclerView.Adapter<TantanganBonusAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJudul: TextView = view.findViewById(R.id.tvJudulBonus)
        val tvDeskripsi: TextView = view.findViewById(R.id.tvDeskripsiBonus)
        val btnAksi: Button = view.findViewById(R.id.btnAksiBonus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tantangan_bonus, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val context = holder.itemView.context

        holder.tvJudul.text = item.judul
        holder.tvDeskripsi.text = item.deskripsi

        val isSelesai = TantanganStatusHelper.isTantanganBonusSelesai(context, item.materiId, item.quizId)

        if (isSelesai) {
            holder.btnAksi.text = "Selesai ✓"
            holder.btnAksi.isEnabled = false
            
            // Berikan badge jika belum punya
            // Karena badgeId sekarang Int, kita cek != 0 dan convert ke String untuk BadgeHelper
            if (item.badgeId != 0 && !BadgeHelper.punyaBadge(context, item.badgeId.toString())) {
                BadgeHelper.tambahBadge(context, item.badgeId.toString())
            }
        } else {
            holder.btnAksi.text = "Mulai"
            holder.btnAksi.isEnabled = true
            holder.btnAksi.setOnClickListener { onMulai(item) }
        }
    }

    override fun getItemCount() = list.size
}