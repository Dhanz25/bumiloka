package com.faiz.bumiloka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class MisiFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_misi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnMulaiMateri = view.findViewById<MaterialButton>(R.id.btnMulaiMateri)

        btnMulaiMateri.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, JelajahiMateriFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}