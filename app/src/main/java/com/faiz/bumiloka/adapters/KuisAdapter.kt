package com.faiz.bumiloka.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.model.Kuis
import com.faiz.bumiloka.R

class KuisAdapter(
    private val list: MutableList<Kuis>,
    private val onEdit: (Kuis) -> Unit,
    private val onDelete: (Kuis) -> Unit
) : RecyclerView.Adapter<KuisAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPertanyaan: TextView = view.findViewById(R.id.tv_pertanyaan)
        val tvKategori: TextView = view.findViewById(R.id.tv_kategori_kuis)
        val tvJawaban: TextView = view.findViewById(R.id.tv_jawaban_benar)
        val tvPoin: TextView = view.findViewById(R.id.tv_target_poin)
        val btnEdit: ImageButton = view.findViewById(R.id.btn_edit_kuis)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_kuis)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kuis, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kuis = list[position]
        holder.tvPertanyaan.text = kuis.pertanyaan
        holder.tvKategori.text = kuis.kategori
        holder.tvJawaban.text = "✓ Jawaban: ${kuis.jawabanBenar}"
        holder.tvPoin.text = "${kuis.poin} poin"
        holder.btnEdit.setOnClickListener { onEdit(kuis) }
        holder.btnDelete.setOnClickListener { onDelete(kuis) }
    }

    override fun getItemCount() = list.size
}