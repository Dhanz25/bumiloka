package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class TantanganFragment : Fragment(R.layout.fragment_tantangan) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<View>(R.id.btnBack)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}