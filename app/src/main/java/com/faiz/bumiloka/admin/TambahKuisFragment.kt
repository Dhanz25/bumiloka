package com.faiz.bumiloka.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.faiz.bumiloka.adapters.SoalInputAdapter
import com.faiz.bumiloka.databinding.FragmentTambahKuisBinding
import com.faiz.bumiloka.model.Kuis
import com.faiz.bumiloka.model.SoalKuis

class TambahKuisFragment : Fragment() {

    private var _binding: FragmentTambahKuisBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private var editId: String? = null
    private lateinit var soalAdapter: SoalInputAdapter

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

        setupLevelSpinner()
        setupRecyclerView()
        observeSoal()

        arguments?.let { args ->
            editId = args.getString("id")
            if (editId != null) {
                binding.toolbar.title = "Edit Kuis"
                binding.etJudul.setText(args.getString("judul"))
                binding.etDeskripsi.setText(args.getString("deskripsi"))
                binding.etPoinReward.setText(args.getInt("poinReward").toString())
                binding.spinnerLevel.setText(args.getInt("level", 1).toString(), false)
                binding.switchAktif.isChecked = args.getBoolean("aktif", true)
                binding.btnSave.text = "UPDATE KUIS & SOAL"
                
                // Fetch existing questions for this quiz
                viewModel.fetchSoal(editId!!)
            }
        }

        binding.btnGenerateSoal.setOnClickListener {
            val countStr = binding.etJumlahSoal.text.toString()
            if (countStr.isNotEmpty()) {
                val count = countStr.toInt()
                if (count in 1..50) {
                    soalAdapter.generateEmptySoal(count)
                    binding.layoutSoalContainer.visibility = View.VISIBLE
                    binding.tvDaftarSoalTitle.text = "Daftar Soal ($count Soal)"
                } else {
                    Toast.makeText(requireContext(), "Jumlah soal antara 1 - 50", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Masukkan jumlah soal", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnTemplateSampah.setOnClickListener {
            applyTemplateSampah()
        }

        binding.btnSave.setOnClickListener { saveKuisLengkap() }
    }

    private fun observeSoal() {
        viewModel.soalList.observe(viewLifecycleOwner) { list ->
            // If in edit mode and we received the list of questions
            if (editId != null && list.isNotEmpty() && soalAdapter.itemCount == 0) {
                binding.etJumlahSoal.setText(list.size.toString())
                binding.layoutSoalContainer.visibility = View.VISIBLE
                binding.tvDaftarSoalTitle.text = "Daftar Soal (${list.size} Soal)"
                soalAdapter.setSoalList(list)
            }
        }
    }

    private fun setupLevelSpinner() {
        val levels = arrayOf("1", "2", "3", "4", "5")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levels)
        binding.spinnerLevel.setAdapter(adapter)
        if (editId == null) {
            binding.spinnerLevel.setText("1", false)
        }
    }

    private fun setupRecyclerView() {
        soalAdapter = SoalInputAdapter()
        binding.rvSoalInput.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = soalAdapter
        }
    }

    private fun applyTemplateSampah() {
        binding.etJudul.setText("Quiz Sampah")
        binding.etDeskripsi.setText("Uji pengetahuanmu tentang pengelolaan sampah.")
        binding.etPoinReward.setText("100")
        binding.spinnerLevel.setText("1", false)
        binding.etJumlahSoal.setText("10")

        val templateSoal = listOf(
            SoalKuis(pertanyaan = "Apa yang dimaksud dengan sampah?", opsiA = "Barang yang sudah tidak terpakai", opsiB = "Barang yang selalu berguna", opsiC = "Makanan sehat", opsiD = "Air bersih", jawabanBenar = "A"),
            SoalKuis(pertanyaan = "Manakah yang termasuk sampah organik?", opsiA = "Botol plastik", opsiB = "Sisa sayuran", opsiC = "Kaleng bekas", opsiD = "Kaca pecah", jawabanBenar = "B"),
            SoalKuis(pertanyaan = "Sampah plastik sebaiknya dikelola dengan cara?", opsiA = "Dibakar", opsiB = "Didaur ulang", opsiC = "Dibuang ke sungai", opsiD = "Ditimbun", jawabanBenar = "B"),
            SoalKuis(pertanyaan = "Apa warna tempat sampah untuk sampah organik?", opsiA = "Merah", opsiB = "Kuning", opsiC = "Hijau", opsiD = "Biru", jawabanBenar = "C"),
            SoalKuis(pertanyaan = "Contoh sampah anorganik adalah?", opsiA = "Daun kering", opsiB = "Kulit buah", opsiC = "Botol kaca", opsiD = "Sisa nasi", jawabanBenar = "C"),
            SoalKuis(pertanyaan = "Berapa lama plastik bisa terurai di tanah?", opsiA = "1 hari", opsiB = "1 minggu", opsiC = "Ratusan tahun", opsiD = "1 tahun", jawabanBenar = "C"),
            SoalKuis(pertanyaan = "Prinsip Reduce dalam 3R berarti?", opsiA = "Mengurangi sampah", opsiB = "Memakai kembali", opsiC = "Mendaur ulang", opsiD = "Membakar sampah", jawabanBenar = "A"),
            SoalKuis(pertanyaan = "Apa dampak membuang sampah ke sungai?", opsiA = "Air jadi jernih", opsiB = "Ikan bertambah", opsiC = "Banjir", opsiD = "Tanah subur", jawabanBenar = "C"),
            SoalKuis(pertanyaan = "Sampah B3 (Berbahaya) contohnya adalah?", opsiA = "Kertas", opsiB = "Baterai bekas", opsiC = "Plastik", opsiD = "Sisa sayuran", jawabanBenar = "B"),
            SoalKuis(pertanyaan = "Mengolah sampah menjadi kompos adalah contoh dari?", opsiA = "Reuse", opsiB = "Reduce", opsiC = "Recycle", opsiD = "Replace", jawabanBenar = "C")
        )
        
        binding.layoutSoalContainer.visibility = View.VISIBLE
        binding.tvDaftarSoalTitle.text = "Daftar Soal (10 Soal)"
        soalAdapter.setSoalList(templateSoal)
        Toast.makeText(requireContext(), "Template Kuis Sampah berhasil diterapkan", Toast.LENGTH_SHORT).show()
    }

    private fun saveKuisLengkap() {
        val judul = binding.etJudul.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()
        val poinReward = binding.etPoinReward.text.toString().trim().toIntOrNull() ?: 0
        val level = binding.spinnerLevel.text.toString().toIntOrNull() ?: 1
        val aktif = binding.switchAktif.isChecked

        if (judul.isEmpty() || deskripsi.isEmpty()) {
            Toast.makeText(requireContext(), "Judul dan deskripsi wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val soalList = soalAdapter.getSoalList()
        if (soalList.isEmpty()) {
            Toast.makeText(requireContext(), "Harap generate soal terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val invalidSoal = soalList.find { it.pertanyaan.isEmpty() || it.opsiA.isEmpty() || it.opsiB.isEmpty() || it.jawabanBenar.isEmpty() }
        
        if (invalidSoal != null) {
            Toast.makeText(requireContext(), "Harap lengkapi semua pertanyaan dan pilihan jawaban", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        val kuis = Kuis(
            id = editId ?: "",
            judul = judul,
            deskripsi = deskripsi,
            level = level,
            poinReward = poinReward,
            aktif = aktif,
            createdAt = if (editId == null) System.currentTimeMillis() else arguments?.getLong("createdAt") ?: System.currentTimeMillis()
        )

        viewModel.saveKuisLengkap(kuis, soalList) { success ->
            if (!isAdded) return@saveKuisLengkap
            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true
            if (success) {
                Toast.makeText(requireContext(), "Kuis dan ${soalList.size} soal berhasil disimpan", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}