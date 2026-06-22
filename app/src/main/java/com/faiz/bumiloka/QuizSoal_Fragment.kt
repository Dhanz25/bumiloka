package com.faiz.bumiloka

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth

class QuizSoalFragment : Fragment(R.layout.fragment_quiz_soal_) {

    private var currentQuestion = 0
    private var skor = 0
    private var sudahPilih = false
    private var selectedAnswer = -1

    private lateinit var tvQuestion: TextView
    private lateinit var tvNumber: TextView
    private lateinit var options: List<TextView>
    private lateinit var btnNext: Button
    
    private var questions: List<Question> = listOf()

    companion object {
        private const val ARG_MATERI_ID = "materi_id"
        fun newInstance(materiId: Int): QuizSoalFragment {
            val fragment = QuizSoalFragment()
            val args = Bundle()
            args.putInt(ARG_MATERI_ID, materiId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

        val materiId = arguments?.getInt("materi_id") ?: 1
        val level = arguments?.getInt("LEVEL") ?: 1
        
        setupQuestions(level, materiId)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        btnNext = view.findViewById(R.id.btnNext)
        tvQuestion = view.findViewById(R.id.tvQuestion)
        tvNumber = view.findViewById(R.id.tvNumber)

        options = listOf(
            view.findViewById(R.id.option1),
            view.findViewById(R.id.option2),
            view.findViewById(R.id.option3),
            view.findViewById(R.id.option4)
        )

        toolbar.setNavigationOnClickListener { showExitDialog() }
        btnNext.isEnabled = false

        loadQuestion()

        btnNext.setOnClickListener {
            if (sudahPilih) {
                if (selectedAnswer == questions[currentQuestion].correctAnswer) {
                    skor += 10
                }
                currentQuestion++
                if (currentQuestion < questions.size) {
                    loadQuestion()
                } else {
                    pindahKeHasil(level, materiId)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
    }

    private fun setupQuestions(level: Int, index: Int) {
        questions = when (level) {
            1 -> getLevel1Questions(index)
            2 -> getLevel2Questions(index)
            3 -> getLevel3Questions(index)
            else -> getLevel1Questions(index)
        }
    }

    private fun getLevel1Questions(index: Int): List<Question> {
        return listOf(
            Question("Apa tindakan paling tepat untuk menjaga kebersihan lingkungan?", listOf("Membuang sampah pada tempatnya", "Membuang sampah ke sungai", "Membakar semua sampah", "Membiarkan sampah menumpuk"), 0),
            Question("Mengapa kita perlu menanam pohon?", listOf("Untuk menambah polusi", "Untuk menghasilkan oksigen", "Untuk mengurangi udara segar", "Untuk mempercepat pemanasan global"), 1),
            Question("Apa yang sebaiknya dilakukan dengan sampah plastik?", listOf("Dibuang ke laut", "Didaur ulang", "Dibakar sembarangan", "Dibiarkan menumpuk"), 1),
            Question("Cara sederhana menghemat listrik di rumah adalah?", listOf("Menyalakan semua lampu", "Mematikan alat listrik saat tidak digunakan", "Membiarkan TV menyala terus", "Menggunakan listrik tanpa batas"), 1),
            Question("Apa dampak membuang sampah sembarangan?", listOf("Lingkungan menjadi bersih", "Terjadi banjir dan pencemaran", "Udara menjadi segar", "Tidak ada dampak"), 1),
            Question("Mengapa kita harus menghemat air?", listOf("Agar air cepat habis", "Agar tersedia untuk masa depan", "Supaya bisa boros", "Agar tidak digunakan orang lain"), 1),
            Question("Apa yang dimaksud dengan daur ulang?", listOf("Membuang sampah", "Mengolah kembali sampah menjadi barang baru", "Membakar sampah", "Menimbun sampah"), 1),
            Question("Contoh energi terbarukan adalah?", listOf("Batu bara", "Minyak bumi", "Energi matahari", "Gas alam"), 2),
            Question("Bagaimana cara menjaga kebersihan sekolah?", listOf("Membuang sampah sembarangan", "Membersihkan kelas secara rutin", "Merusak fasilitas sekolah", "Mencoret-coret dinding"), 1),
            Question("Cara mengurangi penggunaan plastik adalah?", listOf("Menggunakan plastik sekali pakai", "Membawa tas belanja sendiri", "Membuang plastik sembarangan", "Membakar plastik"), 1)
        )
    }

    private fun getLevel2Questions(index: Int): List<Question> {
        return listOf(
            Question("Manakah yang termasuk sampah organik?", listOf("Botol plastik", "Sisa sayuran", "Kaleng bekas", "Kaca pecah"), 1),
            Question("Sampah anorganik sebaiknya dikelola dengan cara?", listOf("Dikubur dalam tanah", "Dibuat kompos", "Didaur ulang", "Dibiarkan saja"), 2),
            Question("Konsep Reduce dalam 3R berarti?", listOf("Mengurangi penggunaan barang sekali pakai", "Menggunakan kembali barang bekas", "Mendaur ulang sampah", "Membeli barang baru"), 0),
            Question("Berapa lama waktu yang dibutuhkan plastik untuk terurai?", listOf("1 tahun", "10 tahun", "Ratusan tahun", "Tidak pernah terurai"), 2),
            Question("Apa manfaat memilah sampah dari rumah?", listOf("Memperbanyak sampah", "Memudahkan proses daur ulang", "Membuat rumah kotor", "Tidak ada manfaat"), 1),
            Question("Sampah kertas termasuk jenis sampah?", listOf("Organik", "Anorganik", "B3", "Cair"), 1),
            Question("Mengapa kita harus menghindari membakar sampah plastik?", listOf("Menghasilkan asap harum", "Melepaskan zat beracun ke udara", "Plastik tidak bisa terbakar", "Membuat plastik awet"), 1),
            Question("Reusable bag digunakan untuk menggantikan?", listOf("Tas kain", "Plastik sekali pakai", "Kardus", "Karung"), 1),
            Question("Sampah B3 (Bahan Berbahaya dan Beracun) contohnya adalah?", listOf("Kulit pisang", "Baterai bekas", "Kertas koran", "Botol minum"), 1),
            Question("Apa tujuan utama dari pengelolaan sampah yang baik?", listOf("Memperindah kota", "Mencegah pencemaran lingkungan", "Mencari keuntungan", "Membuang waktu"), 1)
        )
    }

    private fun getLevel3Questions(index: Int): List<Question> {
        return listOf(
            Question("Mengapa air bersih disebut sumber daya yang terbatas?", listOf("Karena air sangat banyak di laut", "Karena air tawar yang bisa diminum jumlahnya sedikit", "Karena air tidak pernah habis", "Karena air mudah dibuat"), 1),
            Question("Tindakan hemat air saat mencuci tangan adalah?", listOf("Membiarkan keran mengalir terus", "Menutup keran saat menyabuni tangan", "Menggunakan air sebanyak-banyaknya", "Mencuci di sungai"), 1),
            Question("Apa fungsi menanam pohon bagi ketersediaan air?", listOf("Menghabiskan air tanah", "Membantu tanah menyerap air hujan", "Menghalangi air hujan", "Membuat tanah kering"), 1),
            Question("Satu tetes air per detik dari keran bocor bisa membuang air sebanyak?", listOf("1 liter setahun", "10 liter setahun", "Ribuan liter setahun", "Tidak berpengaruh"), 2),
            Question("Manakah cara mandi yang lebih hemat air?", listOf("Menggunakan gayung", "Menggunakan shower", "Berendam di bathtub", "Mandi di kolam"), 1),
            Question("Air bekas cucian beras sebaiknya digunakan untuk?", listOf("Dibuang ke selokan", "Menyiram tanaman", "Mencuci baju", "Memasak lagi"), 1),
            Question("Apa dampak krisis air bersih bagi kesehatan?", listOf("Kulit menjadi bersih", "Meningkatkan risiko penyakit pencernaan", "Tidak ada dampak", "Tubuh lebih segar"), 1),
            Question("Kapan waktu terbaik menyiram tanaman agar air tidak mudah menguap?", listOf("Siang hari terik", "Pagi atau sore hari", "Malam hari gelap", "Setiap jam"), 1),
            Question("Pencemaran air sungai paling banyak disebabkan oleh?", listOf("Ikan", "Limbah industri dan rumah tangga", "Pasir", "Tanaman air"), 1),
            Question("Menggunakan air secukupnya saat mencuci kendaraan adalah bentuk?", listOf("Pemborosan", "Konservasi air", "Ketidaksengajaan", "Kebetulan"), 1)
        )
    }

    private fun loadQuestion() {
        val q = questions[currentQuestion]
        tvNumber.text = "Soal ${currentQuestion + 1}/10"
        tvQuestion.text = q.question
        for (i in options.indices) {
            options[i].text = "${('A' + i)}. ${q.options[i]}"
            options[i].setBackgroundResource(R.drawable.bg_option)
            options[i].setOnClickListener { pilihJawaban(i) }
        }
        sudahPilih = false
        selectedAnswer = -1
        btnNext.isEnabled = false
    }

    private fun pilihJawaban(index: Int) {
        for (option in options) option.setBackgroundResource(R.drawable.bg_option)
        selectedAnswer = index
        sudahPilih = true
        options[index].setBackgroundResource(android.R.color.holo_green_light)
        btnNext.isEnabled = true
    }

    private fun pindahKeHasil(level: Int, index: Int) {
        val bundle = Bundle().apply {
            putInt("BENAR", skor / 10)
            putInt("SALAH", 10 - (skor / 10))
            putInt("SKOR", skor)
            putString("QUIZ_TYPE", "QUIZ$index")
            putAll(arguments ?: Bundle())
        }

        val fragment = QuizMenang1Fragment()
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showExitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.popup_konfirmasikeluar, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(false).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogView.findViewById<Button>(R.id.btnBatal).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnKeluar).setOnClickListener {
            dialog.dismiss()
            parentFragmentManager.popBackStack()
        }
        dialog.show()
    }

    data class Question(val question: String, val options: List<String>, val correctAnswer: Int)
}