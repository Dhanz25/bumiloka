package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class TantanganDiriFragment : Fragment(R.layout.fragment_tantangan_diri) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnMulaiKuis = view.findViewById<MaterialButton>(R.id.btnMulaiKuis)

        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 🟢 langsung aktif
        btnMulaiKuis.isEnabled = true

        btnMulaiKuis.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QuizSoalFragment()) // ✅ langsung ke quiz
                .addToBackStack(null)
                .commit()
        }
    }
}