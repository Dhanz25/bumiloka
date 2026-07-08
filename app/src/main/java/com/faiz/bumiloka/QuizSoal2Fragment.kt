package com.faiz.bumiloka

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.FirebaseDatabase

class QuizSoal2Fragment : Fragment(R.layout.fragment_quiz_soal2) {

    private var currentQuestion = 0
    private var skor = 0
    private var sudahPilih = false
    private var selectedAnswer = -1
    private var poinReward: Int = 20 // Default

    private lateinit var tvQuestion: TextView
    private lateinit var tvNumber: TextView
    private lateinit var options: List<TextView>
    private lateinit var btnNext: Button
    
    private var shuffledQuestions: List<Question> = listOf()

    companion object {
        fun newInstance(level: Int = 1): QuizSoal2Fragment {
            return QuizSoal2Fragment().apply {
                arguments = Bundle().apply { 
                    putInt("LEVEL", level)
                    putString("KUIS_ID", "2") 
                }
            }
        }
    }

    private val questions = listOf(
        Question("Apa yang dimaksud dengan sampah?", listOf("Barang yang sudah tidak terpakai", "Barang yang selalu berguna", "Makanan sehat", "Air bersih"), 0),
        Question("Sampah organik adalah?", listOf("Plastik", "Kaca", "Sisa makanan", "Logam"), 2),
        Question("Contoh sampah anorganik adalah?", listOf("Daun kering", "Sisa nasi", "Plastik", "Kulit buah"), 2),
        Question("Cara mengurangi sampah plastik adalah?", listOf("Menggunakan plastik sekali pakai", "Membawa tas belanja sendiri", "Membuang sampah sembarangan", "Membakar plastik"), 1),
        Question("Sampah yang bisa didaur ulang adalah?", listOf("Plastik dan kertas", "Sisa makanan", "Air kotor", "Tanah"), 0),
        Question("Apa dampak jika sampah menumpuk?", listOf("Lingkungan menjadi bersih", "Banjir dan penyakit", "Udara semakin segar", "Air menjadi jernih"), 1),
        Question("Di mana tempat membuang sampah yang benar?", listOf("Sungai", "Jalan", "Tempat sampah", "Selokan"), 2),
        Question("Apa yang dimaksud dengan daur ulang?", listOf("Membuang sampah", "Mengolah kembali sampah menjadi barang baru", "Membakar sampah", "Menimbun sampah"), 1),
        Question("Mengapa sampah perlu dipilah?", listOf("Agar mudah didaur ulang", "Agar cepat bau", "Agar semakin banyak", "Tidak ada manfaatnya"), 0),
        Question("Sampah berbahaya seperti baterai sebaiknya?", listOf("Dibuang sembarangan", "Dibuang ke tempat khusus", "Dibakar", "Dibuang ke sungai"), 1)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

        val kuisId = arguments?.getString("KUIS_ID") ?: "2"
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
            putString("KUIS_ID", "2")
            putInt("LEVEL", level)
            putInt("POIN_REWARD", poinReward)
            putString("QUIZ_TYPE", "QUIZ2")
            arguments?.let { 
                putString("materi_id", it.getString("materi_id"))
                putString("challenge_id", it.getString("challenge_id"))
            }
        }
        val fragment = QuizMenang1Fragment()
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    data class Question(val question: String, val options: List<String>, val correctAnswer: Int)
}
