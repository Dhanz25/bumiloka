package com.faiz.bumiloka.adapters

import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.faiz.bumiloka.R
import com.faiz.bumiloka.databinding.ItemSoalInputBinding
import com.faiz.bumiloka.model.SoalKuis

class SoalInputAdapter(
    private val onPickImage: (Int) -> Unit
) : RecyclerView.Adapter<SoalInputAdapter.ViewHolder>() {

    private var soalList = mutableListOf<SoalKuis>()

    fun getSoalList(): List<SoalKuis> = soalList

    fun setSoalList(newList: List<SoalKuis>) {
        soalList = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun updateImage(position: Int, base64Image: String) {
        if (position in soalList.indices) {
            soalList[position].imageUrl = base64Image
            notifyItemChanged(position)
        }
    }

    fun generateEmptySoal(count: Int) {
        soalList.clear()
        repeat(count) {
            soalList.add(SoalKuis())
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemSoalInputBinding) : RecyclerView.ViewHolder(binding.root) {
        
        private var pertanyaanWatcher: GenericTextWatcher? = null
        private var opsiAWatcher: GenericTextWatcher? = null
        private var opsiBWatcher: GenericTextWatcher? = null
        private var opsiCWatcher: GenericTextWatcher? = null
        private var opsiDWatcher: GenericTextWatcher? = null

        fun bind(position: Int) {
            val soal = soalList[position]
            binding.tvSoalNumber.text = "Soal #${position + 1}"

            // Remove old watchers to prevent data corruption during recycling
            pertanyaanWatcher?.let { binding.etPertanyaan.removeTextChangedListener(it) }
            opsiAWatcher?.let { binding.etOpsiA.removeTextChangedListener(it) }
            opsiBWatcher?.let { binding.etOpsiB.removeTextChangedListener(it) }
            opsiCWatcher?.let { binding.etOpsiC.removeTextChangedListener(it) }
            opsiDWatcher?.let { binding.etOpsiD.removeTextChangedListener(it) }

            // Set current values
            binding.etPertanyaan.setText(soal.pertanyaan)
            binding.etOpsiA.setText(soal.opsiA)
            binding.etOpsiB.setText(soal.opsiB)
            binding.etOpsiC.setText(soal.opsiC)
            binding.etOpsiD.setText(soal.opsiD)

            // Handle Image Preview (Base64)
            if (soal.imageUrl.isNotEmpty()) {
                if (soal.imageUrl.length > 100) {
                    try {
                        val imageBytes = Base64.decode(soal.imageUrl, Base64.DEFAULT)
                        Glide.with(binding.root.context)
                            .asBitmap()
                            .load(imageBytes)
                            .placeholder(R.drawable.img_lingkungan)
                            .into(binding.ivPreview)
                    } catch (e: Exception) {
                        binding.ivPreview.setImageResource(R.drawable.img_lingkungan)
                    }
                } else {
                    val resId = binding.root.context.resources.getIdentifier(soal.imageUrl, "drawable", binding.root.context.packageName)
                    binding.ivPreview.setImageResource(if (resId != 0) resId else R.drawable.img_lingkungan)
                }
            } else {
                binding.ivPreview.setImageResource(R.drawable.img_lingkungan)
            }

            binding.btnPilihGambar.setOnClickListener { onPickImage(adapterPosition) }

            val options = arrayOf("A", "B", "C", "D")
            val adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_dropdown_item_1line, options)
            binding.spinnerJawaban.setAdapter(adapter)
            binding.spinnerJawaban.setText(soal.jawabanBenar, false)

            // Create and add new watchers
            pertanyaanWatcher = GenericTextWatcher { soalList[adapterPosition].pertanyaan = it }
            opsiAWatcher = GenericTextWatcher { soalList[adapterPosition].opsiA = it }
            opsiBWatcher = GenericTextWatcher { soalList[adapterPosition].opsiB = it }
            opsiCWatcher = GenericTextWatcher { soalList[adapterPosition].opsiC = it }
            opsiDWatcher = GenericTextWatcher { soalList[adapterPosition].opsiD = it }

            binding.etPertanyaan.addTextChangedListener(pertanyaanWatcher)
            binding.etOpsiA.addTextChangedListener(opsiAWatcher)
            binding.etOpsiB.addTextChangedListener(opsiBWatcher)
            binding.etOpsiC.addTextChangedListener(opsiCWatcher)
            binding.etOpsiD.addTextChangedListener(opsiDWatcher)

            binding.spinnerJawaban.setOnItemClickListener { _, _, i, _ ->
                soalList[adapterPosition].jawabanBenar = options[i]
            }
        }
    }

    class GenericTextWatcher(private val onTextChanged: (String) -> Unit) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            onTextChanged(s.toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSoalInputBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = soalList.size
}