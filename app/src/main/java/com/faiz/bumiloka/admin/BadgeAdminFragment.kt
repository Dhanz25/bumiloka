package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.R
import com.faiz.bumiloka.adapters.BadgeAdapter
import com.faiz.bumiloka.model.Badge
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BadgeAdminFragment : Fragment() {

    private lateinit var rvBadge: RecyclerView
    private lateinit var adapter: BadgeAdapter
    private lateinit var tvEmpty: TextView
    private val viewModel: AdminViewModel by viewModels()
    private val badgeList = mutableListOf<Badge>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_badge_admin, container, false)
        
        rvBadge = view.findViewById(R.id.rv_badge)
        tvEmpty = view.findViewById(R.id.tv_empty_badge)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fab_tambah_badge)

        rvBadge.layoutManager = LinearLayoutManager(requireContext())
        setupAdapter()

        fabAdd.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TambahBadgeFragment())
                .addToBackStack(null)
                .commit()
        }

        // Observasi data lencana
        viewModel.badgeList.observe(viewLifecycleOwner) { list ->
            badgeList.clear()
            badgeList.addAll(list)
            adapter.notifyDataSetChanged()
            tvEmpty.visibility = if (badgeList.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.fetchBadges()
        return view
    }

    private fun setupAdapter() {
        adapter = BadgeAdapter(badgeList, 
            onEdit = { badge ->
                val fragment = TambahBadgeFragment()
                val bundle = Bundle().apply {
                    putString("id", badge.id)
                    putString("nama", badge.nama)
                    putString("deskripsi", badge.deskripsi)
                    putString("imageUrl", badge.imageUrl)
                    putInt("level", badge.level)
                }
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onDelete = { badge ->
                showDeleteConfirm(badge)
            }
        )
        rvBadge.adapter = adapter
    }

    private fun showDeleteConfirm(badge: Badge) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Lencana")
            .setMessage("Yakin ingin menghapus lencana '${badge.nama}'?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteBadge(badge.id) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Gagal menghapus (Cek Rules Firebase)", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
