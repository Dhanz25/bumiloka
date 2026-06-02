package com.faiz.bumiloka.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import com.faiz.bumiloka.model.Tantangan

class TambahTantanganFragment : Fragment() {

    private val db = FirebaseDatabase.getInstance().reference.child("tantangan")
    private var editId: String? = null
    private var tanggalMulai: Long = System.currentTimeMillis()
    private var tanggalSelesai: Long = System.currentTimeMillis()
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private lateinit var etJudul: EditText
    private lateinit var etDeskripsi: EditText
    private lateinit var etTargetPoin: EditText
    private lateinit var etHadiah: EditText
    private lateinit var tvTanggalMulai: TextView
    private lateinit var tvTanggalSelesai: TextView
    private lateinit var btnSimpan: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(com.faiz.bumiloka.R.layout.fragment_tambah_tantangan, container, false)

        etJudul = view.findViewById(com.faiz.bumiloka.R.id.et_judul_tantangan)
        etDeskripsi = view.findViewById(com.faiz.bumiloka.R.id.et_deskripsi_tantangan)
        etTargetPoin = view.findViewById(com.faiz.bumiloka.R.id.et_target_poin)
        etHadiah = view.findViewById(com.faiz.bumiloka.R.id.et_hadiah_tantangan)
        tvTanggalMulai = view.findViewById(com.faiz.bumiloka.R.id.tv_tanggal_mulai)
        tvTanggalSelesai = view.findViewById(com.faiz.bumiloka.R.id.tv_tanggal_selesai)
        btnSimpan = view.findViewById(com.faiz.bumiloka.R.id.btn_simpan_tantangan)
        progressBar = view.findViewById(com.faiz.bumiloka.R.id.progress_tambah_tantangan)
        tvTitle = view.findViewById(com.faiz.bumiloka.R.id.tv_title_tambah_tantangan)

        arguments?.let { args ->
            editId = args.getString("id")
            if (editId != null) {
                tvTitle.text = "Edit Tantangan"
                btnSimpan.text = "Update"
                etJudul.setText(args.getString("judul"))
                etDeskripsi.setText(args.getString("deskripsi"))
                etTargetPoin.setText(args.getInt("targetPoin", 0).toString())
                etHadiah.setText(args.getString("hadiah"))
                tanggalMulai = args.getLong("tanggalMulai", System.currentTimeMillis())
                tanggalSelesai = args.getLong("tanggalSelesai", System.currentTimeMillis())
                tvTanggalMulai.text = sdf.format(Date(tanggalMulai))
                tvTanggalSelesai.text = sdf.format(Date(tanggalSelesai))
            }
        }

        tvTanggalMulai.setOnClickListener {
            showDatePicker { ts ->
                tanggalMulai = ts
                tvTanggalMulai.text = sdf.format(Date(ts))
            }
        }
        tvTanggalSelesai.setOnClickListener {
            showDatePicker { ts ->
                tanggalSelesai = ts
                tvTanggalSelesai.text = sdf.format(Date(ts))
            }
        }

        btnSimpan.setOnClickListener { simpanTantangan() }
        return view
    }

    private fun showDatePicker(onSelected: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val selected = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onSelected(selected.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun simpanTantangan() {
        val judul = etJudul.text.toString().trim()
        val deskripsi = etDeskripsi.text.toString().trim()
        val targetPoin = etTargetPoin.text.toString().trim().toIntOrNull() ?: 0
        val hadiah = etHadiah.text.toString().trim()

        if (judul.isEmpty()) { etJudul.error = "Judul tidak boleh kosong"; return }
        if (deskripsi.isEmpty()) { etDeskripsi.error = "Deskripsi tidak boleh kosong"; return }
        if (targetPoin <= 0) { etTargetPoin.error = "Target poin harus lebih dari 0"; return }
        if (tanggalSelesai <= tanggalMulai) {
            Toast.makeText(requireContext(), "Tanggal selesai harus setelah tanggal mulai", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnSimpan.isEnabled = false

        val tantangan = Tantangan(
            id = editId ?: "",
            judul = judul,
            deskripsi = deskripsi,
            targetPoin = targetPoin,
            hadiah = hadiah,
            tanggalMulai = tanggalMulai,
            tanggalSelesai = tanggalSelesai,
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