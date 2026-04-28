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

class QuizSoal2Fragment : Fragment(R.layout.fragment_quiz_soal2) {

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

        fun newInstance(materiId: Int): QuizSoal2Fragment {
            val fragment = QuizSoal2Fragment()
            val args = Bundle()
            args.putInt(ARG_MATERI_ID, materiId)
            fragment.arguments = args
            return fragment
        }
    }

    // ✅ SOAL HEMAT AIR
    private val questions = listOf(

        Question(
            "Apa yang dimaksud dengan sampah?",
            listOf(
                "Barang yang sudah tidak terpakai",
                "Barang yang selalu berguna",
                "Makanan sehat",
                "Air bersih"
            ),
            0
        ),

        Question(
            "Sampah organik adalah?",
            listOf(
                "Plastik",
                "Kaca",
                "Sisa makanan",
                "Logam"
            ),
            2
        ),

        Question(
            "Contoh sampah anorganik adalah?",
            listOf(
                "Daun kering",
                "Sisa nasi",
                "Plastik",
                "Kulit buah"
            ),
            2
        ),

        Question(
            "Cara mengurangi sampah plastik adalah?",
            listOf(
                "Menggunakan plastik sekali pakai",
                "Membawa tas belanja sendiri",
                "Membuang sampah sembarangan",
                "Membakar plastik"
            ),
            1
        ),

        Question(
            "Sampah yang bisa didaur ulang adalah?",
            listOf(
                "Plastik dan kertas",
                "Sisa makanan",
                "Air kotor",
                "Tanah"
            ),
            0
        ),

        Question(
            "Apa dampak jika sampah menumpuk?",
            listOf(
                "Lingkungan menjadi bersih",
                "Banjir dan penyakit",
                "Udara semakin segar",
                "Air menjadi jernih"
            ),
            1
        ),

        Question(
            "Di mana tempat membuang sampah yang benar?",
            listOf(
                "Sungai",
                "Jalan",
                "Tempat sampah",
                "Selokan"
            ),
            2
        ),

        Question(
            "Apa yang dimaksud dengan daur ulang?",
            listOf(
                "Membuang sampah",
                "Mengolah kembali sampah menjadi barang baru",
                "Membakar sampah",
                "Menimbun sampah"
            ),
            1
        ),

        Question(
            "Mengapa sampah perlu dipilah?",
            listOf(
                "Agar mudah didaur ulang",
                "Agar cepat bau",
                "Agar semakin banyak",
                "Tidak ada manfaatnya"
            ),
            0
        ),

        Question(
            "Sampah berbahaya seperti baterai sebaiknya?",
            listOf(
                "Dibuang sembarangan",
                "Dibuang ke tempat khusus",
                "Dibakar",
                "Dibuang ke sungai"
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

        // ✅ BACK + KONFIRMASI
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
        val prefs = requireActivity().getSharedPreferences("KUIS_$userId", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("quiz2_selesai", true)
            .putInt("quiz2_nilai", skor)
            .apply()

        val bundle = Bundle()
        bundle.putInt("BENAR", benar)
        bundle.putInt("SALAH", salah)
        bundle.putInt("SKOR", skor)
        bundle.putString("QUIZ_TYPE", "QUIZ2")

        val fragment = QuizMenang1Fragment()
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
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