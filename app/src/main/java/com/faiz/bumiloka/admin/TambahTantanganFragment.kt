package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.faiz.bumiloka.model.Tantangan
import com.faiz.bumiloka.R

class TambahTantanganFragment : Fragment() {

    private val db = FirebaseDatabase.getInstance().reference.child("tantangan")
    private var editId: String? = null

    private lateinit var etJudul: EditText
    private lateinit var etDeskripsi: EditText
    private lateinit var etBadgeId: EditText
    private lateinit var etMateriId: EditText
    private lateinit var etQuizId: EditText
    private lateinit var btnSimpan: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tambah_tantangan, container, false)

        etJudul = view.findViewById(R.id.et_judul_tantangan)
        etDeskripsi = view.findViewById(R.id.et_deskripsi_tantangan)
        etBadgeId = view.findViewById(R.id.et_badge_id)
        etMateriId = view.findViewById(R.id.et_materi_id)
        etQuizId = view.findViewById(R.id.et_quiz_id)
        btnSimpan = view.findViewById(R.id.btn_simpan_tantangan)
        progressBar = view.findViewById(R.id.progress_tambah_tantangan)
        tvTitle = view.findViewById(R.id.tv_title_tambah_tantangan)

        arguments?.let { args ->
            editId = args.getString("id")
            if (editId != null) {
                tvTitle.text = "Edit Tantangan Bonus"
                btnSimpan.text = "Update"
                etJudul.setText(args.getString("judul"))
                etDeskripsi.setText(args.getString("deskripsi"))
                etBadgeId.setText(args.getString("badgeId"))
                etMateriId.setText(args.getString("materiId"))
                etQuizId.setText(args.getString("quizId"))
            }
        }

        btnSimpan.setOnClickListener { simpanTantangan() }
        return view
    }

    private fun simpanTantangan() {
        val judul = etJudul.text.toString().trim()
        val deskripsi = etDeskripsi.text.toString().trim()
        val badgeId = etBadgeId.text.toString().trim()
        val materiId = etMateriId.text.toString().trim()
        val quizId = etQuizId.text.toString().trim()

        if (judul.isEmpty()) { etJudul.error = "Judul tidak boleh kosong"; return }
        if (deskripsi.isEmpty()) { etDeskripsi.error = "Deskripsi tidak boleh kosong"; return }
        if (badgeId.isEmpty()) { etBadgeId.error = "Badge ID tidak boleh kosong"; return }

        progressBar.visibility = View.VISIBLE
        btnSimpan.isEnabled = false

        val tantangan = Tantangan(
            id = editId ?: "",
            judul = judul,
            deskripsi = deskripsi,
            badgeId = badgeId,
            materiId = materiId,
            quizId = quizId,
            aktif = true,
            createdAt = System.currentTimeMillis()
        )

        val task = if (editId != null) {
            db.child(editId!!).setValue(tantangan)
        } else {
            val newRef = db.push()
            newRef.setValue(tantangan.copy(id = newRef.key ?: ""))
        }

        task.addOnSuccessListener {
            progressBar.visibility = View.GONE
            val msg = if (editId != null) "Tantangan diupdate!" else "Tantangan ditambahkan!"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }.addOnFailureListener { e ->
            progressBar.visibility = View.GONE
            btnSimpan.isEnabled = true
            Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}