package com.faiz.bumiloka

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AktivitasAdapter(
    private val context: Context,
    private val aktivitasList: List<AktivitasItem>
) : RecyclerView.Adapter<AktivitasAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvKategori: TextView = view.findViewById(R.id.tvKategori)
        val tvIsi: TextView = view.findViewById(R.id.tvIsi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_aktivitas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = aktivitasList[position]

        holder.tvKategori.text = item.kategori
        holder.tvIsi.text = item.aktivitasList.joinToString("\n▣ ", prefix = "▣ ")
    }

    override fun getItemCount(): Int = aktivitasList.size
}