package com.faiz.bumiloka.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.faiz.bumiloka.R
import com.faiz.bumiloka.databinding.ItemEdukasiAdminBinding
import com.faiz.bumiloka.model.Edukasi
import android.util.Base64

class AdminEdukasiAdapter(
    private val onEdit: (Edukasi) -> Unit,
    private val onDelete: (Edukasi) -> Unit,
    private val onDetail: (Edukasi) -> Unit
) : RecyclerView.Adapter<AdminEdukasiAdapter.ViewHolder>() {

    private var list = listOf<Edukasi>()

    fun submitList(newList: List<Edukasi>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemEdukasiAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Edukasi) {
            binding.tvTitle.text = item.title
            binding.tvDesc.text = item.description
            binding.chipStatus.text = if (item.aktif) "Aktif" else "Non-aktif"
            
            val context = binding.root.context
            
            if (!item.imageUrl.isNullOrEmpty()) {
                if (item.imageUrl.length > 100) {
                    // Handle Base64
                    try {
                        val imageBytes = Base64.decode(item.imageUrl, Base64.DEFAULT)
                        Glide.with(context)
                            .asBitmap()
                            .load(imageBytes)
                            .placeholder(R.drawable.img_lingkungan)
                            .into(binding.ivEdukasi)
                    } catch (e: Exception) {
                        binding.ivEdukasi.setImageResource(R.drawable.img_lingkungan)
                    }
                } else {
                    // Handle Drawable Name
                    val resId = context.resources.getIdentifier(
                        item.imageUrl, 
                        "drawable", 
                        context.packageName
                    )
                    if (resId != 0) {
                        binding.ivEdukasi.setImageResource(resId)
                    } else {
                        binding.ivEdukasi.setImageResource(R.drawable.img_lingkungan)
                    }
                }
            } else {
                binding.ivEdukasi.setImageResource(R.drawable.img_lingkungan)
            }

            binding.btnMore.setOnClickListener {
                val popup = PopupMenu(context, it)
                popup.menuInflater.inflate(R.menu.menu_admin_item, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> onEdit(item)
                        R.id.action_delete -> onDelete(item)
                        R.id.action_detail -> onDetail(item)
                    }
                    true
                }
                popup.show()
            }
            
            binding.root.setOnClickListener { onDetail(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemEdukasiAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size
}
