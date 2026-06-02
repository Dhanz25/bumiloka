package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.R
import com.faiz.bumiloka.model.Kuis
import com.google.firebase.database.FirebaseDatabase

class TambahKuisFragment : Fragment() {

    private val db = FirebaseDatabase.getInstance().reference.child("kuis")
    private var editId: String? = null

    private lateinit var etPertanyaan: EditText
    private lateinit var etOpsiA: EditText
    private lateinit var etOpsiB: EditText
    private lateinit var etOpsiC: EditText
    private lateinit var etOpsiD: EditText
    private lateinit var spinnerJawaban: Spinner
    private lateinit var etKategori: EditText
    private lateinit var etPoin: EditText
    private lateinit var btnSimpan: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tambah_kuis, container, false)

        etPertanyaan = view.findViewById(R.id.et_pertanyaan)
        etOpsiA = view.findViewById(R.id.et_opsi_a)
        etOpsiB = view.findViewById(R.id.et_opsi_b)
        etOpsiC = view.findViewById(R.id.et_opsi_c)
        etOpsiD = view.findViewById(R.id.et_opsi_d)
        spinnerJawaban = view.findViewById(R.id.spinner_jawaban_benar)
        etKategori = view.findViewById(R.id.et_kategori_kuis)
        etPoin = view.findViewById(R.id.et_poin_kuis)
        btnSimpan = view.findViewById(R.id.btn_simpan_kuis)
        progressBar = view.findViewById(R.id.progress_tambah_kuis)
        tvTitle = view.findViewById(R.id.tv_title_tambah_kuis)

        val opsiJawaban = arrayOf("A", "B", "C", "D")
        spinnerJawaban.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            opsiJawaban
        )

        arguments?.let { args ->
            editId = args.getString("id")
            if (editId != null) {
                tvTitle.text = "Edit Kuis"
                btnSimpan.text = "Update"
                etPertanyaan.setText(args.getString("pertanyaan"))
                etOpsiA.setText(args.getString("opsiA"))
                etOpsiB.setText(args.getString("opsiB"))
                etOpsiC.setText(args.getString("opsiC"))
                etOpsiD.setText(args.getString("opsiD"))
                etKategori.setText(args.getString("kategori"))
                etPoin.setText(args.getInt("poin", 10).toString())
                val idx = opsiJawaban.indexOf(args.getString("jawabanBenar", "A"))
                spinnerJawaban.setSelection(if (idx >= 0) idx else 0)
            }
        }

        btnSimpan.setOnClickListener { simpanKuis() }
        return view
    }

    private fun simpanKuis() {
        val pertanyaan = etPertanyaan.text.toString().trim()
        val opsiA = etOpsiA.text.toString().trim()
        val opsiB = etOpsiB.text.toString().trim()
        val opsiC = etOpsiC.text.toString().trim()
        val opsiD = etOpsiD.text.toString().trim()
        val jawaban = spinnerJawaban.selectedItem.toString()
        val kategori = etKategori.text.toString().trim()
        val poin = etPoin.text.toString().trim().toIntOrNull() ?: 10

        if (pertanyaan.isEmpty()) { etPertanyaan.error = "Pertanyaan tidak boleh kosong"; return }
        if (opsiA.isEmpty()) { etOpsiA.error = "Opsi A wajib diisi"; return }
        if (opsiB.isEmpty()) { etOpsiB.error = "Opsi B wajib diisi"; return }
        if (opsiC.isEmpty()) { etOpsiC.error = "Opsi C wajib diisi"; return }
        if (opsiD.isEmpty()) { etOpsiD.error = "Opsi D wajib diisi"; return }
        if (kategori.isEmpty()) { etKategori.error = "Kategori tidak boleh kosong"; return }

        progressBar.visibility = View.VISIBLE
        btnSimpan.isEnabled = false

        val kuis = Kuis(
            id = editId ?: "",
            pertanyaan = pertanyaan,
            opsiA = opsiA,
            opsiB = opsiB,
            opsiC = opsiC,
            opsiD = opsiD,
            jawabanBenar = jawaban,
            kategori = kategori,
            poin = poin,
            createdAt = System.currentTimeMillis()
        )

        val task = if (editId != null) {
            db.child(editId!!).setValue(kuis)
        } else {
            val newRef = db.push()
            newRef.setValue(kuis.copy(id = newRef.key ?: ""))
        }

        task.addOnSuccessListener {
            progressBar.visibility = View.GONE
            val msg = if (editId != null) "Kuis berhasil diupdate!" else "Kuis berhasil ditambahkan!"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }.addOnFailureListener { e ->
            progressBar.visibility = View.GONE
            btnSimpan.isEnabled = true
            Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}