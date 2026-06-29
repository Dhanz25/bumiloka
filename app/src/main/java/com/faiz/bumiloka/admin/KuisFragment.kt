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
import com.faiz.bumiloka.adapters.AdminKuisAdapter
import com.faiz.bumiloka.databinding.FragmentKuisBinding
import com.faiz.bumiloka.model.Kuis

class KuisFragment : Fragment() {

    private var _binding: FragmentKuisBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: AdminKuisAdapter
    private var fullList = listOf<Kuis>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKuisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        
        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        binding.fabTambahKuis.setOnClickListener {
            navigateToTambah(null)
        }

        viewModel.fetchKuis()
    }

    private fun setupRecyclerView() {
        adapter = AdminKuisAdapter(
            onEdit = { navigateToTambah(it) },
            onDelete = { showDeleteConfirmation(it) }
        )
        binding.rvKuis.layoutManager = LinearLayoutManager(requireContext())
        binding.rvKuis.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterList(query)
                return true
            }

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
            fullList.filter { it.judul.contains(query, ignoreCase = true) }
        }
        adapter.submitList(filtered)
        binding.layoutEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun observeViewModel() {
        viewModel.kuisList.observe(viewLifecycleOwner) { list ->
            fullList = list.sortedByDescending { it.createdAt }
            val currentQuery = binding.searchView.query.toString()
            if (currentQuery.isEmpty()) {
                adapter.submitList(fullList)
                binding.layoutEmpty.visibility = if (fullList.isEmpty()) View.VISIBLE else View.GONE
                binding.rvKuis.visibility = if (fullList.isEmpty()) View.GONE else View.VISIBLE
            } else {
                filterList(currentQuery)
            }
        }
    }

    private fun showDeleteConfirmation(kuis: Kuis) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Kuis")
            .setMessage("Yakin ingin menghapus kuis \"${kuis.judul}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteKuis(kuis.id) { success ->
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

    private fun navigateToTambah(kuis: Kuis?) {
        val fragment = TambahKuisFragment().apply {
            arguments = Bundle().apply {
                kuis?.let {
                    putString("id", it.id)
                    putString("edukasiId", it.edukasiId)
                    putString("judul", it.judul)
                    putString("deskripsi", it.deskripsi)
                    putString("imageUrl", it.imageUrl)
                    putInt("poinReward", it.poinReward)
                    putInt("level", it.level)
                    putBoolean("aktif", it.aktif)
                    putLong("createdAt", it.createdAt)
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
