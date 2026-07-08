package com.faiz.bumiloka

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.FirebaseDatabase

class QuizSoal3Fragment : Fragment(R.layout.fragment_quiz_soal3) {

    private var currentQuestion = 0
    private var skor = 0
    private var sudahPilih = false
    private var selectedAnswer = -1
    private var poinReward: Int = 20 // Default

    private var shuffledQuestions: List<Question> = listOf()

    private lateinit var tvQuestion: TextView
    private lateinit var tvNumber: TextView
    private lateinit var options: List<TextView>
    private lateinit var btnNext: Button

    companion object {
        fun newInstance(level: Int = 1): QuizSoal3Fragment {
            return QuizSoal3Fragment().apply {
                arguments = Bundle().apply { 
                    putInt("LEVEL", level)
                    putString("KUIS_ID", "3") 
                }
            }
        }
    }

    private val questions = listOf(
        Question("Mengapa kita harus menghemat air?", listOf("Agar air cepat habis", "Agar tersedia untuk masa depan", "Agar bisa boros", "Agar tidak digunakan orang lain"), 1),
        Question("Contoh perilaku hemat air di rumah adalah?", listOf("Membiarkan keran terbuka", "Menggunakan air secukupnya", "Menyiram air terus-menerus", "Membuang air bersih"), 1),
        Question("Apa yang harus dilakukan saat melihat keran bocor?", listOf("Dibiarkan saja", "Segera diperbaiki", "Dibuka lebih besar", "Ditambah airnya"), 1),
        Question("Menggosok gigi sebaiknya dilakukan dengan cara?", listOf("Keran tetap menyala", "Menggunakan air secukupnya", "Menghabiskan banyak air", "Menyiram terus-menerus"), 1),
        Question("Air hujan dapat dimanfaatkan untuk?", listOf("Dibuang saja", "Menyiram tanaman", "Mengotori lingkungan", "Dibiarkan menggenang"), 1),
        Question("Dampak jika kita boros air adalah?", listOf("Air semakin banyak", "Kekurangan air bersih", "Lingkungan bersih", "Tidak ada dampak"), 1),
        Question("Cara menghemat air saat mandi adalah?", listOf("Mandi terlalu lama", "Menggunakan air secukupnya", "Membiarkan air mengalir terus", "Mengisi bak penuh setiap saat"), 1),
        Question("Mengapa air bersih harus dijaga?", listOf("Karena tidak penting", "Karena terbatas jumlahnya", "Karena mudah didapat", "Karena bisa dibuang"), 1),
        Question("Kegiatan yang boros air adalah?", listOf("Menutup keran setelah dipakai", "Mencuci kendaraan dengan selang terus menyala", "Menggunakan air secukupnya", "Menampung air hujan"), 1),
        Question("Cara sederhana menjaga ketersediaan air adalah?", listOf("Menebang pohon", "Menanam pohon", "Membuang air bersih", "Mengotori sungai"), 1)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

        val kuisId = arguments?.getString("KUIS_ID") ?: "3"
        fetchPoinReward(kuisId)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        btnNext = view.findViewById(R.id.btnNext)
        tvQuestion = view.findViewById(R.id.tvQuestion)
        tvNumber = view.findViewById(R.id.tvNumber)

        options = listOf(
            view.findViewById(R.id.option1), view.findViewById(R.id.option2),
            view.findViewById(R.id.option3), view.findViewById(R.id.option4)
        )

        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        
        shuffledQuestions = questions.shuffled()
        loadQuestion()

        btnNext.setOnClickListener {
            if (sudahPilih) {
                if (selectedAnswer == shuffledQuestions[currentQuestion].correctAnswer) skor += 10
                currentQuestion++
                if (currentQuestion < shuffledQuestions.size) loadQuestion() else pindahKeHasil()
            }
        }
    }

    private fun fetchPoinReward(id: String) {
        FirebaseDatabase.getInstance().getReference("kuis").child(id).child("poinReward")
            .get().addOnSuccessListener {
                poinReward = it.getValue(Int::class.java) ?: 20
            }
    }

    private fun loadQuestion() {
        if (!isAdded) return
        val q = shuffledQuestions[currentQuestion]
        tvNumber.text = "Soal ${currentQuestion + 1}/${shuffledQuestions.size}"
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
        if (!isAdded) return
        for (option in options) option.setBackgroundResource(R.drawable.bg_option)
        selectedAnswer = index
        sudahPilih = true
        options[index].setBackgroundResource(android.R.color.holo_green_light)
        btnNext.isEnabled = true
    }

    private fun pindahKeHasil() {
        if (!isAdded) return
        val level = arguments?.getInt("LEVEL") ?: 1
        val bundle = Bundle().apply {
            putInt("BENAR", skor / 10)
            putInt("SALAH", shuffledQuestions.size - (skor / 10))
            putInt("SKOR", skor)
            putString("KUIS_ID", "3")
            putInt("LEVEL", level)
            putInt("POIN_REWARD", poinReward)
            putString("QUIZ_TYPE", "QUIZ3")
            arguments?.let { 
                putString("materi_id", it.getString("materi_id"))
                putString("challenge_id", it.getString("challenge_id"))
                putBoolean("DARI_MISI", it.getBoolean("DARI_MISI", false))
            }
        }
        val fragment = QuizMenang1Fragment()
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    data class Question(val question: String, val options: List<String>, val correctAnswer: Int)
}
