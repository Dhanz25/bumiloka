package com.faiz.bumiloka.adapters

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        val ivBanner: ImageView = view.findViewById(R.id.ivBannerBonus)
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
        
        // Pemuatan Gambar
        if (item.imageUrl.isNotEmpty()) {
            when {
                item.imageUrl.startsWith("http") -> {
                    Glide.with(context)
                        .load(item.imageUrl)
                        .placeholder(R.drawable.img_lingkungan)
                        .error(R.drawable.img_lingkungan)
                        .into(holder.ivBanner)
                }
                item.imageUrl.length > 100 -> {
                    try {
                        val imageBytes = Base64.decode(item.imageUrl, Base64.DEFAULT)
                        Glide.with(context)
                            .asBitmap()
                            .load(imageBytes)
                            .placeholder(R.drawable.img_lingkungan)
                            .into(holder.ivBanner)
                    } catch (e: Exception) {
                        holder.ivBanner.setImageResource(R.drawable.img_lingkungan)
                    }
                }
                else -> {
                    val resId = context.resources.getIdentifier(item.imageUrl, "drawable", context.packageName)
                    if (resId != 0) {
                        holder.ivBanner.setImageResource(resId)
                    } else {
                        holder.ivBanner.setImageResource(R.drawable.img_lingkungan)
                    }
                }
            }
        } else {
            holder.ivBanner.setImageResource(R.drawable.img_lingkungan)
        }

        // Cek status selesai menggunakan String IDs
        val isSelesai = TantanganStatusHelper.isTantanganSelesai(context, item.id)

        if (isSelesai) {
            holder.btnAksi.text = "Selesai ✓"
            holder.btnAksi.isEnabled = false
            if (item.badgeId.isNotEmpty() && !BadgeHelper.punyaBadge(context, item.badgeId)) {
                BadgeHelper.tambahBadge(context, item.badgeId)
            }
        } else {
            holder.btnAksi.text = "Mulai"
            holder.btnAksi.isEnabled = true
            holder.btnAksi.setOnClickListener { onMulai(item) }
        }
    }

    override fun getItemCount() = list.size
}
