package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.faiz.bumiloka.databinding.FragmentTambahEdukasiBinding
import com.faiz.bumiloka.model.Edukasi

class TambahEdukasiFragment : Fragment() {

    private var _binding: FragmentTambahEdukasiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private var editId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahEdukasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        arguments?.let { args ->
            editId = args.getString("id")
            if (editId != null) {
                binding.toolbar.title = "Edit Materi Edukasi"
                binding.etTitle.setText(args.getString("title"))
                binding.etDescription.setText(args.getString("description"))
                binding.etContent.setText(args.getString("content"))
                binding.etImageUrl.setText(args.getString("imageUrl"))
                binding.etBadgeName.setText(args.getString("badgeName"))
                binding.etBadgeImage.setText(args.getString("badgeImage"))
                binding.switchAktif.isChecked = args.getBoolean("aktif", true)
                binding.btnSave.text = "UPDATE MATERI"
            }
        }

        binding.btnSave.setOnClickListener { saveEdukasi() }
    }

    private fun saveEdukasi() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val imageUrl = binding.etImageUrl.text.toString().trim()
        val badgeName = binding.etBadgeName.text.toString().trim()
        val badgeImage = binding.etBadgeImage.text.toString().trim()
        val aktif = binding.switchAktif.isChecked

        if (title.isEmpty() || description.isEmpty() || content.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi semua field wajib", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        val edukasi = Edukasi(
            id = editId ?: "",
            title = title,
            description = description,
            content = content,
            imageUrl = imageUrl,
            badgeName = badgeName,
            badgeImage = badgeImage,
            aktif = aktif,
            createdAt = if (editId == null) System.currentTimeMillis() else arguments?.getLong("createdAt") ?: System.currentTimeMillis()
        )

        viewModel.saveEdukasi(edukasi) { success ->
            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true
            if (success) {
                Toast.makeText(requireContext(), "Berhasil disimpan", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Gagal menyimpan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
