package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class TantanganPenjelajahMingguanFragment : Fragment(R.layout.fragment_tantangan_master_kuis) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔻 Sembunyikan Bottom Navigation
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.GONE

        // 🔙 Tombol Back
        val btnBack = view.findViewById<View>(R.id.btnBack)

        // 🔘 Tombol Mulai Materi
        val btnMulaiMateri = view.findViewById<View>(R.id.btnMulaiMateri)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnMulaiMateri.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EdukasiFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 🔺 Tampilkan kembali Bottom Navigation
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.VISIBLE
    }
}