package com.faiz.bumiloka.adapters

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.faiz.bumiloka.R
import com.faiz.bumiloka.databinding.ItemKuisAdminBinding
import com.faiz.bumiloka.model.Kuis

class AdminKuisAdapter(
    private val onEdit: (Kuis) -> Unit,
    private val onDelete: (Kuis) -> Unit
) : RecyclerView.Adapter<AdminKuisAdapter.ViewHolder>() {

    private var list = listOf<Kuis>()

    fun submitList(newList: List<Kuis>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemKuisAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Kuis) {
            binding.tvTitle.text = item.judul
            binding.tvDesc.text = item.deskripsi
            binding.chipPoin.text = "${item.poinReward} Poin"
            binding.chipLevel.text = "Lvl ${item.level}"
            binding.chipStatus.text = if (item.aktif) "Aktif" else "Non-aktif"
            
            val context = binding.root.context
            
            // Pemuatan Gambar Dinamis (Base64 Galeri atau Drawable Lokal)
            if (!item.imageUrl.isNullOrEmpty()) {
                if (item.imageUrl.length > 100) {
                    // Kasus: Gambar dari Galeri (Base64)
                    try {
                        val imageBytes = Base64.decode(item.imageUrl, Base64.DEFAULT)
                        Glide.with(context)
                            .asBitmap()
                            .load(imageBytes)
                            .placeholder(R.drawable.img_lingkungan)
                            .error(R.drawable.img_lingkungan)
                            .into(binding.ivKuis)
                    } catch (e: Exception) {
                        binding.ivKuis.setImageResource(R.drawable.img_lingkungan)
                    }
                } else {
                    // Kasus: Nama File Drawable
                    val resId = context.resources.getIdentifier(item.imageUrl, "drawable", context.packageName)
                    if (resId != 0) {
                        binding.ivKuis.setImageResource(resId)
                    } else {
                        binding.ivKuis.setImageResource(R.drawable.img_lingkungan)
                    }
                }
            } else {
                // Kasus: Tidak ada gambar
                binding.ivKuis.setImageResource(R.drawable.img_lingkungan)
            }

            binding.btnMore.setOnClickListener {
                val popup = PopupMenu(binding.root.context, it)
                popup.menuInflater.inflate(R.menu.menu_admin_kuis, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> onEdit(item)
                        R.id.action_delete -> onDelete(item)
                    }
                    true
                }
                popup.show()
            }
            
            binding.root.setOnClickListener { onEdit(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemKuisAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size
}
