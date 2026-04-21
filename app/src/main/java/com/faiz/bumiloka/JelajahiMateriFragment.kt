package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment

class JelajahiMateriFragment : Fragment(R.layout.fragment_jelajahi_materi) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnLanjut = view.findViewById<Button>(R.id.btnLanjut)

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