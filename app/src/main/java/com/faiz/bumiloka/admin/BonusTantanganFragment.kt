package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.R
import com.faiz.bumiloka.adapters.BonusTantanganAdapter
import com.faiz.bumiloka.model.BonusChallengeModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BonusTantanganFragment : Fragment() {

    private val viewModel: AdminViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BonusTantanganAdapter
    private lateinit var tvEmpty: TextView
    private val bonusList = mutableListOf<BonusChallengeModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bonus_tantangan, container, false)

        tvEmpty = view.findViewById(R.id.tv_empty)
        recyclerView = view.findViewById(R.id.rv_bonus_tantangan)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = BonusTantanganAdapter(
            bonusList,
            onEdit = { navigateToTambah(it) },
            onDelete = { deleteBonus(it) }
        )
        recyclerView.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.fab_tambah).setOnClickListener {
            navigateToTambah(null)
        }

        observeViewModel()
        viewModel.fetchBonusTantangan()

        return view
    }

    private fun observeViewModel() {
        viewModel.bonusTantanganList.observe(viewLifecycleOwner) { list ->
            bonusList.clear()
            bonusList.addAll(list.sortedByDescending { it.createdAt })
            adapter.notifyDataSetChanged()
            tvEmpty.visibility = if (bonusList.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun deleteBonus(item: BonusChallengeModel) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Hapus Tantangan Bonus")
            .setMessage("Yakin ingin menghapus \"${item.judul}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteBonusTantangan(item.id) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Gagal menghapus", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun navigateToTambah(item: BonusChallengeModel?) {
        val fragment = TambahBonusTantanganFragment().apply {
            arguments = Bundle().apply {
                item?.let {
                    putString("id", it.id)
                    putString("judul", it.judul)
                    putString("deskripsi", it.deskripsi)
                    putBoolean("aktif", it.aktif)
                }
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
