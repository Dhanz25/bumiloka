package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment

class QuizMenang2Fragment : Fragment(R.layout.fragment_quiz_menang2_) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnOk = view.findViewById<Button>(R.id.btnOk)
        val ivBack = view.findViewById<ImageView>(R.id.ivBack)

        btnOk.setOnClickListener {
            val fm = requireActivity().supportFragmentManager

            fm.popBackStack() // keluar dari hasil
        }

        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}