package com.faiz.bumiloka.adapters

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
    private val onDelete: (Kuis) -> Unit,
    private val onManageSoal: (Kuis) -> Unit
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
            binding.chipStatus.text = if (item.aktif) "Aktif" else "Non-aktif"
            
            Glide.with(binding.root)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(binding.ivKuis)

            binding.btnMore.setOnClickListener {
                val popup = PopupMenu(binding.root.context, it)
                popup.menuInflater.inflate(R.menu.menu_admin_kuis, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_manage_soal -> onManageSoal(item)
                        R.id.action_edit -> onEdit(item)
                        R.id.action_delete -> onDelete(item)
                    }
                    true
                }
                popup.show()
            }
            
            binding.root.setOnClickListener { onManageSoal(item) }
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
