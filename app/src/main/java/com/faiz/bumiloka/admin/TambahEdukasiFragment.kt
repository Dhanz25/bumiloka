package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.R
import com.faiz.bumiloka.model.Edukasi
import com.google.firebase.database.FirebaseDatabase

class TambahEdukasiFragment : Fragment() {

    private val db = FirebaseDatabase.getInstance().reference.child("materi")
    private var editId: String? = null

    private lateinit var etJudul: EditText
    private lateinit var etKonten: EditText
    private lateinit var etKategori: EditText
    private lateinit var etImageUrl: EditText
    private lateinit var btnSimpan: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tambah_edukasi, container, false)

        etJudul = view.findViewById(R.id.et_judul_edukasi)
        etKonten = view.findViewById(R.id.et_konten_edukasi)
        etKategori = view.findViewById(R.id.et_kategori_edukasi)
        etImageUrl = view.findViewById(R.id.et_image_url_edukasi)
        btnSimpan = view.findViewById(R.id.btn_simpan_edukasi)
        progressBar = view.findViewById(R.id.progress_tambah_edukasi)
        tvTitle = view.findViewById(R.id.tv_title_tambah_edukasi)

        // Cek mode edit
        arguments?.let { args ->
            editId = args.getString("id")
            if (editId != null) {
                tvTitle.text = "Edit Materi"
                btnSimpan.text = "Update"
                etJudul.setText(args.getString("judul"))
                etKonten.setText(args.getString("konten"))
                etKategori.setText(args.getString("kategori"))
                etImageUrl.setText(args.getString("imageUrl"))
            }
        }

        btnSimpan.setOnClickListener { simpanMateri() }
        return view
    }

    private fun simpanMateri() {
        val judul = etJudul.text.toString().trim()
        val konten = etKonten.text.toString().trim()
        val kategori = etKategori.text.toString().trim()
        val imageUrl = etImageUrl.text.toString().trim()

        if (judul.isEmpty()) { etJudul.error = "Judul tidak boleh kosong"; return }
        if (konten.isEmpty()) { etKonten.error = "Konten tidak boleh kosong"; return }
        if (kategori.isEmpty()) { etKategori.error = "Kategori tidak boleh kosong"; return }

        progressBar.visibility = View.VISIBLE
        btnSimpan.isEnabled = false

        val materi = Edukasi(
            id = editId ?: "",
            judul = judul,
            konten = konten,
            kategori = kategori,
            imageUrl = imageUrl,
            createdAt = System.currentTimeMillis()
        )

        val task = if (editId != null) {
            // Mode Update: gunakan ID yang sudah ada
            db.child(editId!!).setValue(materi)
        } else {
            // Mode Tambah: push() buat ID unik otomatis
            val newRef = db.push()
            newRef.setValue(materi.copy(id = newRef.key ?: ""))
        }

        task.addOnSuccessListener {
            progressBar.visibility = View.GONE
            val msg = if (editId != null) "Materi berhasil diupdate!" else "Materi berhasil ditambahkan!"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }.addOnFailureListener { e ->
            progressBar.visibility = View.GONE
            btnSimpan.isEnabled = true
            Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}