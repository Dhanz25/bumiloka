package com.faiz.bumiloka

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class Jelajahi_MateriDetail : Fragment(R.layout.fragment_jelajahi__materi_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val checkSelesai = view.findViewById<CheckBox>(R.id.checkSelesai)
        val btnSelesai = view.findViewById<Button>(R.id.btnSelesai)

        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        checkSelesai.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                btnSelesai.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.nav_active))
                btnSelesai.isEnabled = true
            } else {
                btnSelesai.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                btnSelesai.isEnabled = false
            }
        }

        btnSelesai.isEnabled = false

        btnSelesai.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Misi Selesai")
                .setMessage("Selamat Anda telah menyelesaikan Misi")
                .setCancelable(false)
                .setPositiveButton("Lanjutkan") { _, _ ->
                    requireActivity().supportFragmentManager.popBackStack()
                }
                .show()
        }
    }
}