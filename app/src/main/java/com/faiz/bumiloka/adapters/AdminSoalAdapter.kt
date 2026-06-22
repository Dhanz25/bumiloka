package com.faiz.bumiloka.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.R
import com.faiz.bumiloka.databinding.ItemSoalAdminBinding
import com.faiz.bumiloka.model.SoalKuis

class AdminSoalAdapter(
    private val onEdit: (SoalKuis) -> Unit,
    private val onDelete: (SoalKuis) -> Unit
) : RecyclerView.Adapter<AdminSoalAdapter.ViewHolder>() {

    private var list = listOf<SoalKuis>()

    fun submitList(newList: List<SoalKuis>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemSoalAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SoalKuis) {
            binding.tvPertanyaan.text = item.pertanyaan
            binding.tvJawaban.text = "Jawaban Benar: ${item.jawabanBenar}"
            
            binding.btnMore.setOnClickListener {
                val popup = PopupMenu(binding.root.context, it)
                popup.menuInflater.inflate(R.menu.menu_admin_item, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> onEdit(item)
                        R.id.action_delete -> onDelete(item)
                    }
                    true
                }
                popup.menu.findItem(R.id.action_detail)?.isVisible = false
                popup.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSoalAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size
}
