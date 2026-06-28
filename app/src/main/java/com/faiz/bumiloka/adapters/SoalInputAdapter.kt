package com.faiz.bumiloka.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.databinding.ItemSoalInputBinding
import com.faiz.bumiloka.model.SoalKuis

class SoalInputAdapter : RecyclerView.Adapter<SoalInputAdapter.ViewHolder>() {

    private var soalList = mutableListOf<SoalKuis>()

    fun getSoalList(): List<SoalKuis> = soalList

    fun setSoalList(newList: List<SoalKuis>) {
        soalList = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun generateEmptySoal(count: Int) {
        soalList.clear()
        repeat(count) {
            soalList.add(SoalKuis())
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemSoalInputBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val soal = soalList[position]
            binding.tvSoalNumber.text = "Soal #${position + 1}"

            // Remove previous listeners to avoid conflicts when rebinding
            binding.etPertanyaan.text = null
            binding.etOpsiA.text = null
            binding.etOpsiB.text = null
            binding.etOpsiC.text = null
            binding.etOpsiD.text = null

            binding.etPertanyaan.setText(soal.pertanyaan)
            binding.etOpsiA.setText(soal.opsiA)
            binding.etOpsiB.setText(soal.opsiB)
            binding.etOpsiC.setText(soal.opsiC)
            binding.etOpsiD.setText(soal.opsiD)

            val options = arrayOf("A", "B", "C", "D")
            val adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_dropdown_item_1line, options)
            binding.spinnerJawaban.setAdapter(adapter)
            binding.spinnerJawaban.setText(soal.jawabanBenar, false)

            binding.etPertanyaan.doAfterTextChanged { soalList[position].pertanyaan = it.toString() }
            binding.etOpsiA.doAfterTextChanged { soalList[position].opsiA = it.toString() }
            binding.etOpsiB.doAfterTextChanged { soalList[position].opsiB = it.toString() }
            binding.etOpsiC.doAfterTextChanged { soalList[position].opsiC = it.toString() }
            binding.etOpsiD.doAfterTextChanged { soalList[position].opsiD = it.toString() }
            binding.spinnerJawaban.setOnItemClickListener { _, _, i, _ ->
                soalList[position].jawabanBenar = options[i]
            }
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