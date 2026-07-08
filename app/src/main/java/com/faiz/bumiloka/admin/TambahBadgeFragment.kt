package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.faiz.bumiloka.BadgeVisualHelper
import com.faiz.bumiloka.R
import com.faiz.bumiloka.model.Badge

class TambahBadgeFragment : Fragment() {

    private val viewModel: AdminViewModel by viewModels()
    private var editId: String? = null

    private lateinit var etNama: EditText
    private lateinit var etDeskripsi: EditText
    private lateinit var spinnerLevel: AutoCompleteTextView
    private lateinit var ivPreview: ImageView
    private lateinit var btnSimpan: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView

    private var selectedLevel: Int = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tambah_badge, container, false)

        etNama = view.findViewById(R.id.et_nama_badge)
        etDeskripsi = view.findViewById(R.id.et_deskripsi_badge)
        spinnerLevel = view.findViewById(R.id.spinner_level_badge)
        ivPreview = view.findViewById(R.id.iv_preview_badge)
        btnSimpan = view.findViewById(R.id.btn_simpan_badge)
        progressBar = view.findViewById(R.id.progress_tambah_badge)
        tvTitle = view.findViewById(R.id.tv_title_tambah_badge)

        setupLevelSpinner()

        // REAL-TIME RENDERING: Langsung menggambar lencana saat Nama diketik
        etNama.addTextChangedListener { text ->
            updatePreview()
        }

        arguments?.let { args ->
            editId = args.getString("id")
            if (editId != null) {
                tvTitle.text = "Edit Lencana"
                btnSimpan.text = "Update Lencana"
                etNama.setText(args.getString("nama"))
                etDeskripsi.setText(args.getString("deskripsi"))
                selectedLevel = args.getInt("level", 1)
                
                val levelText = when(selectedLevel) {
                    0 -> "Umum"
                    else -> "Level $selectedLevel"
                }
                spinnerLevel.setText(levelText, false)
                updatePreview()
            }
        }

        // Render awal jika data baru
        if (editId == null) {
            updatePreview()
        }

        btnSimpan.setOnClickListener { simpanBadge() }
        return view
    }

    private fun updatePreview() {
        // Menggunakan BadgeVisualHelper untuk menggambar lencana secara dinamis
        BadgeVisualHelper.renderBadge(ivPreview, etNama.text.toString(), selectedLevel)
    }

    private fun setupLevelSpinner() {
        val levels = listOf("Level 1", "Level 2", "Level 3", "Umum")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levels)
        spinnerLevel.setAdapter(adapter)
        spinnerLevel.setOnItemClickListener { _, _, position, _ ->
            selectedLevel = if (position == 3) 0 else position + 1
            // Langsung update preview saat level diubah (Bentuk lencana akan berubah)
            updatePreview()
        }
    }

    private fun simpanBadge() {
        val nama = etNama.text.toString().trim()
        val desc = etDeskripsi.text.toString().trim()

        if (nama.isEmpty()) { etNama.error = "Nama wajib diisi"; return }

        progressBar.visibility = View.VISIBLE
        btnSimpan.isEnabled = false

        val badge = Badge(
            id = editId ?: "",
            nama = nama,
            deskripsi = desc,
            imageUrl = "", // Tidak perlu simpan image path karena ikon digambar dinamis dari Nama + Level
            level = selectedLevel,
            createdAt = System.currentTimeMillis()
        )

        viewModel.saveBadge(badge) { success ->
            if (isAdded) {
                if (success) {
                    Toast.makeText(requireContext(), "Lencana berhasil disimpan", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    progressBar.visibility = View.GONE
                    btnSimpan.isEnabled = true
                    Toast.makeText(requireContext(), "Gagal Simpan: Aturan Firebase (Rules) ditolak", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
