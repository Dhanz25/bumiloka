package com.faiz.bumiloka.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.faiz.bumiloka.R
import com.faiz.bumiloka.databinding.ItemEdukasiAdminBinding
import com.faiz.bumiloka.model.Edukasi

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
            
            Glide.with(binding.root)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(binding.ivEdukasi)

            binding.btnMore.setOnClickListener {
                val popup = PopupMenu(binding.root.context, it)
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
