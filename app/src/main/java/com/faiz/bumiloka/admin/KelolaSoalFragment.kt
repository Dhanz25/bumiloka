package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.faiz.bumiloka.R
import com.faiz.bumiloka.adapters.AdminSoalAdapter
import com.faiz.bumiloka.databinding.FragmentKelolaSoalBinding
import com.faiz.bumiloka.model.SoalKuis

class KelolaSoalFragment : Fragment() {

    private var _binding: FragmentKelolaSoalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: AdminSoalAdapter
    private var kuisId: String = ""
    private var kuisJudul: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            kuisId = it.getString("kuisId") ?: ""
            kuisJudul = it.getString("kuisJudul") ?: "Kuis"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaSoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.toolbar.title = "Kelola Soal"
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        binding.tvKuisJudul.text = "Kuis: $kuisJudul"

        setupRecyclerView()
        observeViewModel()

        binding.fabAdd.setOnClickListener {
            navigateToTambah(null)
        }

        if (kuisId.isNotEmpty()) {
            viewModel.fetchSoal(kuisId)
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminSoalAdapter(
            onEdit = { navigateToTambah(it) },
            onDelete = { showDeleteConfirmation(it) }
        )
        binding.rvSoal.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSoal.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.soalList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showDeleteConfirmation(soal: SoalKuis) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Soal")
            .setMessage("Yakin ingin menghapus soal ini?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteSoal(kuisId, soal.id) { success ->
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

    private fun navigateToTambah(soal: SoalKuis?) {
        val fragment = TambahSoalFragment().apply {
            arguments = Bundle().apply {
                putString("kuisId", kuisId)
                soal?.let {
                    putString("id", it.id)
                    putString("pertanyaan", it.pertanyaan)
                    putString("opsiA", it.opsiA)
                    putString("opsiB", it.opsiB)
                    putString("opsiC", it.opsiC)
                    putString("opsiD", it.opsiD)
                    putString("jawabanBenar", it.jawabanBenar)
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
