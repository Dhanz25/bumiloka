package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.faiz.bumiloka.R
import com.faiz.bumiloka.adapters.AdminEdukasiAdapter
import com.faiz.bumiloka.databinding.FragmentEdukasiAdminBinding
import com.faiz.bumiloka.model.Edukasi

class EdukasiFragment : Fragment() {

    private var _binding: FragmentEdukasiAdminBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: AdminEdukasiAdapter
    private var fullList = listOf<Edukasi>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEdukasiAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        binding.fabAdd.setOnClickListener {
            navigateToTambah(null)
        }

        viewModel.fetchEdukasi()
    }

    private fun setupRecyclerView() {
        adapter = AdminEdukasiAdapter(
            onEdit = { navigateToTambah(it) },
            onDelete = { showDeleteConfirmation(it) },
            onDetail = { showDetail(it) }
        )
        binding.rvEdukasi.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEdukasi.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        val filtered = if (query.isNullOrBlank()) {
            fullList
        } else {
            fullList.filter { it.title.contains(query, ignoreCase = true) }
        }
        adapter.submitList(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun observeViewModel() {
        viewModel.edukasiList.observe(viewLifecycleOwner) { list ->
            fullList = list.sortedByDescending { it.createdAt }
            filterList(binding.searchView.query.toString())
        }
    }

    private fun showDeleteConfirmation(edukasi: Edukasi) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Materi")
            .setMessage("Yakin ingin menghapus \"${edukasi.title}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteEdukasi(edukasi.id) { success ->
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

    private fun showDetail(edukasi: Edukasi) {
        val detailView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_jelajahi__materi_detail, null)
        // Set data to detailView here if needed, but since the request asks for "Detail Materi" feature,
        // using a dialog or a new fragment is fine. Let's just use a simple AlertDialog for detail for now or navigate.
        // The prompt says "Detail Materi" as part of CRUD.
        
        AlertDialog.Builder(requireContext())
            .setTitle(edukasi.title)
            .setMessage("Deskripsi: ${edukasi.description}\n\nKonten: ${edukasi.content}\n\nBadge: ${edukasi.badgeName}\n\nAktif: ${edukasi.aktif}")
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun navigateToTambah(edukasi: Edukasi?) {
        val fragment = TambahEdukasiFragment().apply {
            arguments = Bundle().apply {
                edukasi?.let {
                    putString("id", it.id)
                    putString("title", it.title)
                    putString("description", it.description)
                    putString("content", it.content)
                    putString("imageUrl", it.imageUrl)
                    putString("badgeName", it.badgeName)
                    putString("badgeImage", it.badgeImage)
                    putBoolean("aktif", it.aktif)
                }
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
