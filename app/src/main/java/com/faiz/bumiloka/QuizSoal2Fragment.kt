package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class QuizSoal2Fragment : Fragment(R.layout.fragment_quiz_soal_) {

    private var skor = 0
    private lateinit var options: List<TextView>
    private var sudahPilih = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivBack = view.findViewById<ImageView>(R.id.ivBack)
        val btnNext = view.findViewById<Button>(R.id.btnNext)

        btnNext.isEnabled = false

        options = listOf(
            view.findViewById(R.id.option1),
            view.findViewById(R.id.option2),
            view.findViewById(R.id.option3),
            view.findViewById(R.id.option4)
        )

        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        options[0].setOnClickListener { selectOption(0, 100, btnNext) }
        options[1].setOnClickListener { selectOption(1, 75, btnNext) }
        options[2].setOnClickListener { selectOption(2, 50, btnNext) }
        options[3].setOnClickListener { selectOption(3, 25, btnNext) }

        btnNext.setOnClickListener {
            if (sudahPilih) {
                pindahKeHasil()
            }
        }
    }

    private fun selectOption(index: Int, points: Int, btnNext: Button) {
        resetOptions()
        skor = points
        sudahPilih = true

        btnNext.isEnabled = true
        options[index].setBackgroundResource(android.R.color.holo_green_light)
    }

    private fun resetOptions() {
        for (option in options) {
            option.setBackgroundResource(R.drawable.bg_option)
        }
    }

    private fun pindahKeHasil() {

        val prefs = requireActivity().getSharedPreferences("KUIS", 0)
        val editor = prefs.edit()

        // 🔥 INI BEDANYA (KUIS 2)
        editor.putBoolean("quiz2_selesai", true)
        editor.putInt("quiz2_nilai", skor)
        editor.apply()

        val fragment = if (skor == 100) {
            QuizMenang2Fragment()
        } else {
            QuizMenang1Fragment()
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}