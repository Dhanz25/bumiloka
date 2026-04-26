package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment

class QuizMenang1Fragment : Fragment(R.layout.fragment_quiz_menang1_) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnOk = view.findViewById<Button>(R.id.btnOk)
        val btnUlangi = view.findViewById<Button>(R.id.btnUlangi)

        val quizType = arguments?.getString("QUIZ_TYPE")

        // ✅ OK → balik ke halaman utama
        btnOk.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QuizUtamaFragment())
                .commit()
        }

        // 🔁 ULANGI → kembali ke soal kuis 1
        btnUlangi.setOnClickListener {
            val fragment = when (quizType) {
                "QUIZ1" -> QuizSoalFragment()
                "QUIZ2" -> QuizSoal2Fragment()
                "QUIZ3" -> QuizSoal3Fragment()
                else -> QuizSoalFragment()
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}