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
import com.faiz.bumiloka.model.Edukasi
import com.faiz.bumiloka.adapters.MateriAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EdukasiFragment : Fragment() {

    private val db = FirebaseDatabase.getInstance().reference.child("materi")
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MateriAdapter
    private lateinit var tvEmpty: TextView
    private val materiList = mutableListOf<Edukasi>()
    private var dbListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Changed from fragment_materi to fragment_edukasi2 for admin list view
        val view = inflater.inflate(R.layout.fragment_edukasi2, container, false)

        tvEmpty = view.findViewById(R.id.tv_empty_edukasi)
        recyclerView = view.findViewById(R.id.rv_edukasi)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = MateriAdapter(
            materiList,
            onEdit = { materi -> navigateToTambah(materi) },
            onDelete = { materi -> deleteMateri(materi) }
        )
        recyclerView.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.fab_tambah_edukasi).setOnClickListener {
            navigateToTambah(null)
        }

        loadMateri()
        return view
    }

    private fun loadMateri() {
        dbListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                materiList.clear()
                for (child in snapshot.children) {
                    val materi = child.getValue(Edukasi::class.java)?.copy(id = child.key ?: "")
                    materi?.let { materiList.add(it) }
                }
                materiList.sortByDescending { it.createdAt }
                adapter.notifyDataSetChanged()
                tvEmpty.visibility = if (materiList.isEmpty()) View.VISIBLE else View.GONE
                recyclerView.visibility = if (materiList.isEmpty()) View.GONE else View.VISIBLE
            }
            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        db.addValueEventListener(dbListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbListener?.let { db.removeEventListener(it) }
    }

    private fun deleteMateri(materi: Edukasi) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Hapus Materi")
            .setMessage("Yakin ingin menghapus \"${materi.judul}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                db.child(materi.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Materi berhasil dihapus", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Gagal menghapus", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun navigateToTambah(materi: Edukasi?) {
        val fragment = TambahEdukasiFragment().apply {
            arguments = Bundle().apply {
                materi?.let {
                    putString("id", it.id)
                    putString("judul", it.judul)
                    putString("konten", it.konten)
                    putString("kategori", it.kategori)
                    putString("imageUrl", it.imageUrl)
                }
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}