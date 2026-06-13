package com.faiz.bumiloka.adapters
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.R
import com.faiz.bumiloka.model.MateriLingkunganJawa
import android.widget.ImageView
import com.bumptech.glide.Glide

class MateriLingkunganAdapter(
    private val listMateri: List<MateriLingkunganJawa>
) : RecyclerView.Adapter<MateriLingkunganAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val imgMateri: ImageView =
            itemView.findViewById(R.id.imgMateri)
        val tvNama: TextView =
            itemView.findViewById(R.id.tvNama)

        val tvDeskripsi: TextView =
            itemView.findViewById(R.id.tvDeskripsi)

//        val tvIcon: TextView =
//            itemView.findViewById(R.id.tvIcon)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_materi_lingkungan,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val materi = listMateri[position]

        holder.tvNama.text = materi.nama
        holder.tvDeskripsi.text = materi.deskripsi

        Glide.with(holder.itemView.context)
            .load(materi.gambar)
            .centerCrop()
            .into(holder.imgMateri)

//        holder.tvIcon.text = when (position % 3) {
//            0 -> "🌳"
//            1 -> "🍃"
//            else -> "🌿"
//        }
    }

    override fun getItemCount(): Int {
        return listMateri.size
    }
}