package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.R
import com.faiz.bumiloka.model.Kuis
import com.faiz.bumiloka.adapters.KuisAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class KuisFragment : Fragment() {

    private val db = FirebaseDatabase.getInstance().reference.child("kuis")
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: KuisAdapter
    private lateinit var tvEmpty: TextView
    private val kuisList = mutableListOf<Kuis>()
    private var dbListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_kuis, container, false)

        tvEmpty = view.findViewById(R.id.tv_title_kuis)
        recyclerView = view.findViewById(R.id.rv_kuis)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = KuisAdapter(
            kuisList,
            onEdit = { kuis -> navigateToTambah(kuis) },
            onDelete = { kuis -> deleteKuis(kuis) }
        )
        recyclerView.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.fab_tambah_kuis).setOnClickListener {
            navigateToTambah(null)
        }

        loadKuis()
        return view
    }

    private fun loadKuis() {
        dbListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                kuisList.clear()
                for (child in snapshot.children) {
                    val kuis = child.getValue(Kuis::class.java)?.copy(id = child.key ?: "")
                    kuis?.let { kuisList.add(it) }
                }
                kuisList.sortByDescending { it.createdAt }
                adapter.notifyDataSetChanged()
                tvEmpty.visibility = if (kuisList.isEmpty()) View.VISIBLE else View.GONE
                recyclerView.visibility = if (kuisList.isEmpty()) View.GONE else View.VISIBLE
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

    private fun deleteKuis(kuis: Kuis) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Hapus Kuis")
            .setMessage("Yakin ingin menghapus pertanyaan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                db.child(kuis.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Kuis berhasil dihapus", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun navigateToTambah(kuis: Kuis?) {
        val fragment = TambahKuisFragment().apply {
            arguments = Bundle().apply {
                kuis?.let {
                    putString("id", it.id)
                    putString("pertanyaan", it.pertanyaan)
                    putString("opsiA", it.opsiA)
                    putString("opsiB", it.opsiB)
                    putString("opsiC", it.opsiC)
                    putString("opsiD", it.opsiD)
                    putString("jawabanBenar", it.jawabanBenar)
                    putString("kategori", it.kategori)
                    putInt("poin", it.poin)
                }
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}