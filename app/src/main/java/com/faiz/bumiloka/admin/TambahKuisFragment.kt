package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.faiz.bumiloka.databinding.FragmentTambahKuisBinding
import com.faiz.bumiloka.model.Kuis

class TambahKuisFragment : Fragment() {

    private var _binding: FragmentTambahKuisBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private var editId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahKuisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        arguments?.let { args ->
            editId = args.getString("id")
            if (editId != null) {
                binding.toolbar.title = "Edit Kuis"
                binding.etEdukasiId.setText(args.getString("edukasiId"))
                binding.etJudul.setText(args.getString("judul"))
                binding.etDeskripsi.setText(args.getString("deskripsi"))
                binding.etImageUrl.setText(args.getString("imageUrl"))
                binding.etPoinReward.setText(args.getInt("poinReward").toString())
                binding.switchAktif.isChecked = args.getBoolean("aktif", true)
                binding.btnSave.text = "UPDATE KUIS"
            }
        }

        binding.btnSave.setOnClickListener { saveKuis() }
    }

    private fun saveKuis() {
        val edukasiId = binding.etEdukasiId.text.toString().trim()
        val judul = binding.etJudul.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()
        val imageUrl = binding.etImageUrl.text.toString().trim()
        val poinReward = binding.etPoinReward.text.toString().trim().toIntOrNull() ?: 0
        val aktif = binding.switchAktif.isChecked

        if (judul.isEmpty() || deskripsi.isEmpty()) {
            Toast.makeText(requireContext(), "Judul dan deskripsi wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        val kuis = Kuis(
            id = editId ?: "",
            edukasiId = edukasiId,
            judul = judul,
            deskripsi = deskripsi,
            imageUrl = imageUrl,
            poinReward = poinReward,
            aktif = aktif,
            createdAt = if (editId == null) System.currentTimeMillis() else arguments?.getLong("createdAt") ?: System.currentTimeMillis()
        )

        viewModel.saveKuis(kuis) { success ->
            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true
            if (success) {
                Toast.makeText(requireContext(), "Kuis berhasil disimpan", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Gagal menyimpan kuis", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
