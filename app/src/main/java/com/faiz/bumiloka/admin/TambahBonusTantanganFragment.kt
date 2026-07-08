package com.faiz.bumiloka.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.faiz.bumiloka.R
import com.faiz.bumiloka.model.BonusChallengeModel
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputLayout

class TambahBonusTantanganFragment : Fragment() {

    private val viewModel: AdminViewModel by viewModels()
    private var editId: String? = null

    private lateinit var etJudul: EditText
    private lateinit var etDeskripsi: EditText
    private lateinit var swAktif: MaterialSwitch
    private lateinit var btnSimpan: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView
    
    private lateinit var spinnerBadge: AutoCompleteTextView
    private lateinit var spinnerType: AutoCompleteTextView
    private lateinit var spinnerQuiz: AutoCompleteTextView
    private lateinit var spinnerMateri: AutoCompleteTextView
    private lateinit var etTargetDays: EditText
    private lateinit var tilTargetDays: TextInputLayout
    private lateinit var tilQuizId: TextInputLayout

    private var selectedBadgeId = ""
    private var selectedQuizId = ""
    private var selectedMateriId = ""
    private var selectedType = "COMMITMENT"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tambah_bonus_tantangan, container, false)

        initViews(view)
        setupSpinners()
        
        arguments?.let { args ->
            editId = args.getString("id")
            if (editId != null) {
                tvTitle.text = "Edit Tantangan Bonus"
                etJudul.setText(args.getString("judul"))
                etDeskripsi.setText(args.getString("deskripsi"))
                swAktif.isChecked = args.getBoolean("aktif", true)
                
                selectedBadgeId = args.getString("badgeId", "")
                selectedType = args.getString("type", "COMMITMENT")
                etTargetDays.setText(args.getInt("targetDays", 1).toString())
                selectedQuizId = args.getString("quizId", "")
                selectedMateriId = args.getString("materiId", "")
                
                spinnerType.setText(selectedType, false)
                btnSimpan.text = "Update Tantangan"
                
                updateTypeVisibility(selectedType)
            }
        }

        btnSimpan.setOnClickListener { simpanData() }

        return view
    }

    private fun initViews(view: View) {
        etJudul = view.findViewById(R.id.et_judul)
        etDeskripsi = view.findViewById(R.id.et_deskripsi)
        swAktif = view.findViewById(R.id.sw_aktif)
        btnSimpan = view.findViewById(R.id.btn_simpan)
        progressBar = view.findViewById(R.id.progress_bar)
        tvTitle = view.findViewById(R.id.tv_title_tambah)
        
        spinnerBadge = view.findViewById(R.id.spinner_badge)
        spinnerType = view.findViewById(R.id.spinner_type)
        spinnerQuiz = view.findViewById(R.id.spinner_quiz)
        spinnerMateri = view.findViewById(R.id.spinner_materi)
        etTargetDays = view.findViewById(R.id.et_target_days)
        tilTargetDays = view.findViewById(R.id.til_target_days)
        tilQuizId = view.findViewById(R.id.til_quiz_id)
    }

    private fun setupSpinners() {
        val types = arrayOf("COMMITMENT", "QUIZ")
        spinnerType.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types))
        spinnerType.setOnItemClickListener { _, _, position, _ ->
            selectedType = types[position]
            updateTypeVisibility(selectedType)
        }

        viewModel.fetchBadges()
        viewModel.badgeList.observe(viewLifecycleOwner) { badges ->
            val badgeNames = badges.map { it.nama }
            spinnerBadge.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, badgeNames))
            if (selectedBadgeId.isNotEmpty()) {
                badges.find { it.id == selectedBadgeId }?.let { spinnerBadge.setText(it.nama, false) }
            }
            spinnerBadge.setOnItemClickListener { _, _, position, _ -> 
                if (position >= 0) selectedBadgeId = badges[position].id 
            }
        }

        viewModel.fetchKuis()
        viewModel.kuisList.observe(viewLifecycleOwner) { quizzes ->
            val quizNames = quizzes.map { it.judul }
            spinnerQuiz.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, quizNames))
            if (selectedQuizId.isNotEmpty()) {
                quizzes.find { it.id == selectedQuizId }?.let { spinnerQuiz.setText(it.judul, false) }
            }
            spinnerQuiz.setOnItemClickListener { _, _, position, _ -> 
                if (position >= 0) selectedQuizId = quizzes[position].id 
            }
        }

        viewModel.fetchEdukasi()
        viewModel.edukasiList.observe(viewLifecycleOwner) { edukasiList ->
            val materiTitles = edukasiList.map { it.title }
            spinnerMateri.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, materiTitles))
            if (selectedMateriId.isNotEmpty()) {
                edukasiList.find { it.id == selectedMateriId }?.let { spinnerMateri.setText(it.title, false) }
            }
            spinnerMateri.setOnItemClickListener { _, _, position, _ -> 
                if (position >= 0) selectedMateriId = edukasiList[position].id 
            }
        }
    }

    private fun updateTypeVisibility(type: String) {
        if (type == "COMMITMENT") {
            tilTargetDays.visibility = View.VISIBLE
            tilQuizId.visibility = View.GONE
        } else {
            tilTargetDays.visibility = View.GONE
            tilQuizId.visibility = View.VISIBLE
        }
    }

    private fun simpanData() {
        val judul = etJudul.text.toString().trim()
        val deskripsi = etDeskripsi.text.toString().trim()
        val targetDaysStr = etTargetDays.text.toString().trim()
        val targetDays = if (targetDaysStr.isEmpty()) 1 else targetDaysStr.toInt()

        if (judul.isEmpty()) { etJudul.error = "Judul wajib"; return }
        if (deskripsi.isEmpty()) { etDeskripsi.error = "Deskripsi wajib"; return }
        
        // PERBAIKAN: Validasi Lencana diperingatkan tapi jangan langsung mematikan proses jika edit
        if (selectedBadgeId.isEmpty() && editId == null) {
            Toast.makeText(requireContext(), "Silakan pilih Hadiah Lencana", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnSimpan.isEnabled = false

        val bonus = BonusChallengeModel(
            id = editId ?: "",
            judul = judul,
            deskripsi = deskripsi,
            badgeId = selectedBadgeId,
            materiId = selectedMateriId, // Sekarang opsional agar tidak error jika spinner disembunyikan
            type = selectedType,
            targetDays = if (selectedType == "COMMITMENT") targetDays else 1,
            quizId = if (selectedType == "QUIZ") selectedQuizId else "",
            aktif = swAktif.isChecked,
            createdAt = System.currentTimeMillis()
        )

        Log.d("TambahBonus", "Data yang dikirim: $bonus")

        viewModel.saveBonusTantangan(bonus) { success, errorMsg ->
            if (isAdded) {
                progressBar.visibility = View.GONE
                btnSimpan.isEnabled = true
                if (success) {
                    Toast.makeText(requireContext(), "Berhasil disimpan", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Gagal: ${errorMsg ?: "Kesalahan Database"}", Toast.LENGTH_LONG).show()
                    Log.e("TambahBonus", "Gagal Simpan: $errorMsg")
                }
            }
        }
    }
}
