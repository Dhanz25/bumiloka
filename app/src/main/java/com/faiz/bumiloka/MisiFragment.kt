package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class MisiFragment : Fragment(R.layout.fragment_misi) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnMulaiMateri = view.findViewById<MaterialButton>(R.id.btnMulaiMateri)
        val btnTantangan = view.findViewById<MaterialButton>(R.id.btnTantangan)
        val btnSkor = view.findViewById<MaterialButton>(R.id.btnSkor)

        val sharedPref = requireActivity().getSharedPreferences("MISI", 0)
        // 🔥 reset (HANYA UNTUK TESTING)
        sharedPref.edit().putBoolean("misi1_selesai", false).apply()
        val misi1Selesai = sharedPref.getBoolean("misi1_selesai", false)

        // =========================
        // MISI 1
        // =========================
        if (misi1Selesai) {
            // ❌ sudah selesai → tidak bisa diklik
            btnMulaiMateri.isEnabled = false
            btnMulaiMateri.text = "Selesai"

            btnMulaiMateri.setBackgroundColor(
                ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
            )

        } else {
            // ✅ belum selesai → aktif
            btnMulaiMateri.isEnabled = true
            btnMulaiMateri.text = "Mulai"

            btnMulaiMateri.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.nav_active)
            )

            btnMulaiMateri.setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, JelajahiMateriFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        // =========================
        // MISI 2 & 3
        // =========================
        if (misi1Selesai) {
            enableButton(btnTantangan)
            enableButton(btnSkor)
        } else {
            disableButton(btnTantangan)
            disableButton(btnSkor)
        }
    }

    // 🔒 LOCK
    private fun disableButton(button: MaterialButton) {
        button.isEnabled = false
        button.text = "Terkunci"
        button.setBackgroundColor(
            ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        )
        button.alpha = 0.6f
    }

    // 🔓 UNLOCK
    private fun enableButton(button: MaterialButton) {
        button.isEnabled = true
        button.text = "Mulai"
        button.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.nav_active)
        )
        button.alpha = 1f
    }
}