package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.R
import com.faiz.bumiloka.adapters.TantanganAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.faiz.bumiloka.model.Tantangan

class TantanganFragment : Fragment() {

    private val db = FirebaseDatabase.getInstance().reference.child("tantangan")
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TantanganAdapter
    private lateinit var tvEmpty: TextView
    private val tantanganList = mutableListOf<Tantangan>()
    private var dbListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tantangan2, container, false)

        tvEmpty = view.findViewById(R.id.tv_empty_tantangan)
        recyclerView = view.findViewById(R.id.rv_tantangan)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TantanganAdapter(
            tantanganList,
            onEdit = { navigateToTambah(it) },
            onDelete = { deleteTantangan(it) },
            onToggleAktif = { toggleAktif(it) }
        )
        recyclerView.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.fab_tambah_tantangan).setOnClickListener {
            navigateToTambah(null)
        }

        loadTantangan()
        return view
    }

    private fun loadTantangan() {
        dbListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tantanganList.clear()
                for (child in snapshot.children) {
                    val t = child.getValue(Tantangan::class.java)?.copy(id = child.key ?: "")
                    t?.let { tantanganList.add(it) }
                }
                tantanganList.sortByDescending { it.createdAt }
                adapter.notifyDataSetChanged()
                tvEmpty.visibility = if (tantanganList.isEmpty()) View.VISIBLE else View.GONE
                recyclerView.visibility = if (tantanganList.isEmpty()) View.GONE else View.VISIBLE
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        db.addValueEventListener(dbListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbListener?.let { db.removeEventListener(it) }
    }

    private fun deleteTantangan(tantangan: Tantangan) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Hapus Tantangan")
            .setMessage("Yakin ingin menghapus \"${tantangan.judul}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                db.child(tantangan.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Tantangan berhasil dihapus", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun toggleAktif(tantangan: Tantangan) {
        val newStatus = !tantangan.aktif
        db.child(tantangan.id).child("aktif").setValue(newStatus)
            .addOnSuccessListener {
                val status = if (newStatus) "diaktifkan" else "dinonaktifkan"
                Toast.makeText(requireContext(), "Tantangan $status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToTambah(tantangan: Tantangan?) {
        val fragment = TambahTantanganFragment().apply {
            arguments = Bundle().apply {
                tantangan?.let {
                    putString("id", it.id)
                    putString("judul", it.judul)
                    putString("deskripsi", it.deskripsi)
                    putInt("targetPoin", it.targetPoin)
                    putString("hadiah", it.hadiah)
                    putLong("tanggalMulai", it.tanggalMulai)
                    putLong("tanggalSelesai", it.tanggalSelesai)
                }
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}