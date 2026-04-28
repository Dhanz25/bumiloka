package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment

class TipsHematAirFragment : Fragment(R.layout.fragment_tips_hemat_air) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}