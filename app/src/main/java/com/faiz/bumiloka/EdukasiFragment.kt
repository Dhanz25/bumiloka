package com.faiz.bumiloka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class EdukasiFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edukasi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔻 Sembunyikan Bottom Navigation
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.GONE

        // 🔙 Tombol Back
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 🔺 Tampilkan kembali Bottom Navigation saat keluar fragment
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.VISIBLE
    }
}