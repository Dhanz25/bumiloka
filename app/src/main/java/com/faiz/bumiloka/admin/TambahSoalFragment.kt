package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.faiz.bumiloka.databinding.FragmentTambahSoalBinding
import com.faiz.bumiloka.model.SoalKuis

class TambahSoalFragment : Fragment() {

    private var _binding: FragmentTambahSoalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private var kuisId: String = ""
    private var editId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            kuisId = it.getString("kuisId") ?: ""
            editId = it.getString("id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahSoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        val opsiJawaban = arrayOf("A", "B", "C", "D")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, opsiJawaban)
        binding.spinnerJawaban.setAdapter(adapter)

        arguments?.let { args ->
            if (editId != null) {
                binding.toolbar.title = "Edit Soal"
                binding.etPertanyaan.setText(args.getString("pertanyaan"))
                binding.etOpsiA.setText(args.getString("opsiA"))
                binding.etOpsiB.setText(args.getString("opsiB"))
                binding.etOpsiC.setText(args.getString("opsiC"))
                binding.etOpsiD.setText(args.getString("opsiD"))
                binding.spinnerJawaban.setText(args.getString("jawabanBenar"), false)
                binding.btnSave.text = "UPDATE SOAL"
            }
        }

        binding.btnSave.setOnClickListener { saveSoal() }
    }

    private fun saveSoal() {
        val pertanyaan = binding.etPertanyaan.text.toString().trim()
        val opsiA = binding.etOpsiA.text.toString().trim()
        val opsiB = binding.etOpsiB.text.toString().trim()
        val opsiC = binding.etOpsiC.text.toString().trim()
        val opsiD = binding.etOpsiD.text.toString().trim()
        val jawaban = binding.spinnerJawaban.text.toString().trim()

        if (pertanyaan.isEmpty() || opsiA.isEmpty() || opsiB.isEmpty() || jawaban.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi semua field wajib", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        val soal = SoalKuis(
            id = editId ?: "",
            pertanyaan = pertanyaan,
            opsiA = opsiA,
            opsiB = opsiB,
            opsiC = opsiC,
            opsiD = opsiD,
            jawabanBenar = jawaban
        )

        viewModel.saveSoal(kuisId, soal) { success ->
            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true
            if (success) {
                Toast.makeText(requireContext(), "Soal berhasil disimpan", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Gagal menyimpan soal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
