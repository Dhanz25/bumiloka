package com.faiz.bumiloka

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment

class JelajahiMateriFragment : Fragment(R.layout.fragment_jelajahi_materi) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnLanjut = view.findViewById<Button>(R.id.btnLanjut)

        // 🔒 tombol awal tidak bisa diklik
        btnLanjut.isEnabled = false
        btnLanjut.text = "Tunggu..."

        // ⏳ countdown 5 menit (300000 ms)
        object : CountDownTimer(300000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val menit = millisUntilFinished / 1000 / 60
                val detik = (millisUntilFinished / 1000) % 60
                btnLanjut.text = "Tunggu ${menit}:${detik.toString().padStart(2,'0')}"
            }

            override fun onFinish() {
                btnLanjut.isEnabled = true
                btnLanjut.text = "Lanjut"
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