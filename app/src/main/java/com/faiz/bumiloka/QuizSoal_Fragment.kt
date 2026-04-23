package com.faiz.bumiloka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class QuizSoalFragment : Fragment() {

    private var skor = 0
    private lateinit var options: List<TextView>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz_soal_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivBack = view.findViewById<ImageView>(R.id.ivBack)
        val btnNext = view.findViewById<Button>(R.id.btnNext)

        options = listOf(
            view.findViewById(R.id.option1),
            view.findViewById(R.id.option2),
            view.findViewById(R.id.option3),
            view.findViewById(R.id.option4)
        )

        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Pilih jawaban
        options[0].setOnClickListener { selectOption(0, 100) }
        options[1].setOnClickListener { selectOption(1, 75) }
        options[2].setOnClickListener { selectOption(2, 50) }
        options[3].setOnClickListener { selectOption(3, 25) }

        btnNext.setOnClickListener {
            pindahKeHasil()
        }
    }

    private fun selectOption(index: Int, points: Int) {
        resetOptions()
        skor = points
        options[index].setBackgroundResource(android.R.color.holo_green_light)
    }

    private fun resetOptions() {
        for (option in options) {
            option.setBackgroundResource(R.drawable.bg_option)
        }
    }

    private fun pindahKeHasil() {

        val fragment = when {
            skor >= 100 -> QuizMenang1Fragment()
            skor >= 50 -> QuizMenang2Fragment()
            else -> QuizSelesaiFragment()
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}