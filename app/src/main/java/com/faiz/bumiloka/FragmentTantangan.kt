package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import android.widget.Button

class TantanganFragment : Fragment(R.layout.fragment_tantangan) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.GONE

        val btnBack = view.findViewById<View>(R.id.btnBack)
        val btnMulai1 = view.findViewById<Button>(R.id.btnMulai1)
        val btnMulai2 = view.findViewById<Button>(R.id.btnMulai2)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // =========================

// PENJELAJAH MINGGUAN
// ==========================

        if (TantanganStatusHelper.isPenjelajahSelesai(requireContext())) {

            btnMulai1.text = "Selesai ✓"
            btnMulai1.isEnabled = false
        }
        btnMulai1.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    TantanganPenjelajahMingguanFragment()
                )
                .addToBackStack(null)
                .commit()
        }

// MASTER KUIS
// ==========================

        if (TantanganStatusHelper.isMasterKuisSelesai(requireContext())) {

            btnMulai2.text = "Selesai ✓"
            btnMulai2.isEnabled = false
        }
        btnMulai2.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    TantanganMasterKuisFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 🔺 Tampilkan kembali Bottom Navigation saat keluar fragment
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.VISIBLE
    }
}