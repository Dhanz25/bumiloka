package com.faiz.bumiloka

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth

class QuizSoal3Fragment : Fragment(R.layout.fragment_quiz_soal3) {

    private var currentQuestion = 0
    private var skor = 0
    private var sudahPilih = false
    private var selectedAnswer = -1

    private lateinit var tvQuestion: TextView
    private lateinit var tvNumber: TextView
    private lateinit var options: List<TextView>
    private lateinit var btnNext: Button
    companion object {
        private const val ARG_MATERI_ID = "materi_id"

        fun newInstance(materiId: Int): QuizSoal3Fragment {
            val fragment = QuizSoal3Fragment()
            val args = Bundle()
            args.putInt(ARG_MATERI_ID, materiId)
            fragment.arguments = args
            return fragment
        }
    }

    private val questions = listOf(

        Question(
            "Mengapa kita harus menghemat air?",
            listOf(
                "Agar air cepat habis",
                "Agar tersedia untuk masa depan",
                "Agar bisa boros",
                "Agar tidak digunakan orang lain"
            ),
            1
        ),

        Question(
            "Contoh perilaku hemat air di rumah adalah?",
            listOf(
                "Membiarkan keran terbuka",
                "Menggunakan air secukupnya",
                "Menyiram air terus-menerus",
                "Membuang air bersih"
            ),
            1
        ),

        Question(
            "Apa yang harus dilakukan saat melihat keran bocor?",
            listOf(
                "Dibiarkan saja",
                "Segera diperbaiki",
                "Dibuka lebih besar",
                "Ditambah airnya"
            ),
            1
        ),

        Question(
            "Menggosok gigi sebaiknya dilakukan dengan cara?",
            listOf(
                "Keran tetap menyala",
                "Menggunakan air secukupnya",
                "Menghabiskan banyak air",
                "Menyiram terus-menerus"
            ),
            1
        ),

        Question(
            "Air hujan dapat dimanfaatkan untuk?",
            listOf(
                "Dibuang saja",
                "Menyiram tanaman",
                "Mengotori lingkungan",
                "Dibiarkan menggenang"
            ),
            1
        ),

        Question(
            "Dampak jika kita boros air adalah?",
            listOf(
                "Air semakin banyak",
                "Kekurangan air bersih",
                "Lingkungan bersih",
                "Tidak ada dampak"
            ),
            1
        ),

        Question(
            "Cara menghemat air saat mandi adalah?",
            listOf(
                "Mandi terlalu lama",
                "Menggunakan air secukupnya",
                "Membiarkan air mengalir terus",
                "Mengisi bak penuh setiap saat"
            ),
            1
        ),

        Question(
            "Mengapa air bersih harus dijaga?",
            listOf(
                "Karena tidak penting",
                "Karena terbatas jumlahnya",
                "Karena mudah didapat",
                "Karena bisa dibuang"
            ),
            1
        ),

        Question(
            "Kegiatan yang boros air adalah?",
            listOf(
                "Menutup keran setelah dipakai",
                "Mencuci kendaraan dengan selang terus menyala",
                "Menggunakan air secukupnya",
                "Menampung air hujan"
            ),
            1
        ),

        Question(
            "Cara sederhana menjaga ketersediaan air adalah?",
            listOf(
                "Menebang pohon",
                "Menanam pohon",
                "Membuang air bersih",
                "Mengotori sungai"
            ),
            1
        )
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        // 🔥 POPUP KONFIRMASI KELUAR
        toolbar.setNavigationOnClickListener {
            showExitDialog()
        }

        btnNext.isEnabled = false
        loadQuestion()

        btnNext.setOnClickListener {
            if (sudahPilih) {

                val q = questions[currentQuestion]

                if (selectedAnswer == q.correctAnswer) {
                    skor += 10
                }

                currentQuestion++

                if (currentQuestion < questions.size) {
                    loadQuestion()
                } else {
                    pindahKeHasil()
                }
            }
        }
    }

    private fun loadQuestion() {
        val q = questions[currentQuestion]

        tvNumber.text = "Soal ${currentQuestion + 1}/10"
        tvQuestion.text = q.question

        for (i in options.indices) {
            options[i].text = "${('A' + i)}. ${q.options[i]}"
            options[i].setBackgroundResource(R.drawable.bg_option)

            options[i].setOnClickListener {
                pilihJawaban(i)
            }
        }

        sudahPilih = false
        selectedAnswer = -1
        btnNext.isEnabled = false
    }

    private fun pilihJawaban(index: Int) {

        for (option in options) {
            option.setBackgroundResource(R.drawable.bg_option)
        }

        selectedAnswer = index
        sudahPilih = true

        options[index].setBackgroundResource(android.R.color.holo_green_light)
        btnNext.isEnabled = true
    }

    private fun pindahKeHasil() {

        val totalSoal = 10
        val benar = skor / 10
        val salah = totalSoal - benar

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        
        LevelHelper.getCurrentLevel(requireContext()) { levelUser ->
            val prefs = requireActivity().getSharedPreferences("KUIS_${userId}_LEVEL_$levelUser", Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("quiz3_selesai", true)
                .putInt("quiz3_nilai", skor)
                .apply()

            val dariMisi = arguments?.getBoolean("DARI_MISI", false) ?: false
            val bundle = Bundle()
            bundle.putInt("BENAR", benar)
            bundle.putInt("SALAH", salah)
            bundle.putInt("SKOR", skor)
            bundle.putString("QUIZ_TYPE", "QUIZ3")
            bundle.putBoolean("DARI_MISI", dariMisi)

            val fragment = QuizMenang1Fragment()
            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showExitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.popup_konfirmasikeluar, null)

        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val btnKeluar = dialogView.findViewById<Button>(R.id.btnKeluar)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnKeluar.setOnClickListener {
            dialog.dismiss()
            parentFragmentManager.popBackStack()
        }

        dialog.show()
    }

    data class Question(
        val question: String,
        val options: List<String>,
        val correctAnswer: Int
    )
}