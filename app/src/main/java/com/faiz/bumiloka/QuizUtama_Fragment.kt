package com.faiz.bumiloka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class QuizUtamaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz_utama_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnKerjakan2 = view.findViewById<Button>(R.id.btn_kerjakan_materi2)
        val btnKerjakan3 = view.findViewById<Button>(R.id.btn_kerjakan_materi3)

        val btnTabBelum = view.findViewById<TextView>(R.id.tab_belum)
        val btnTabSemua = view.findViewById<TextView>(R.id.tab_all)

        btnKerjakan2.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QuizSoalFragment())
                .addToBackStack(null)
                .commit()
        }

        btnKerjakan3.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QuizSoalFragment())
                .addToBackStack(null)
                .commit()
        }

        btnTabBelum.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QuizBelumFragment())
                .addToBackStack(null)
                .commit()
        }

        btnTabSemua.setOnClickListener {
            // tetap di sini (atau refresh fragment ini)
        }
    }
}