package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class TantanganFragment : Fragment(R.layout.fragment_tantangan) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.GONE

        val btnBack = view.findViewById<View>(R.id.btnBack)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 🔺 Tampilkan kembali Bottom Navigation saat keluar fragment
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.VISIBLE
    }
}