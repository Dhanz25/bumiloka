package com.faiz.bumiloka

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class JelajahiMateriFragment : Fragment(R.layout.fragment_jelajahi_materi) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnLanjut = view.findViewById<Button>(R.id.btnLanjut)

        // 🔒 awal: disable + abu-abu
        btnLanjut.isEnabled = false
        btnLanjut.text = "Tunggu..."
        btnLanjut.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        )

        // ⏳ timer (10 detik)
        object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                // ✅ aktif + hijau
                btnLanjut.isEnabled = true
                btnLanjut.text = "Lanjut"
                btnLanjut.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.nav_active)
                )
            }
        }.start()

        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        btnLanjut.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Jelajahi_MateriDetail())
                .addToBackStack(null)
                .commit()
        }
    }
}