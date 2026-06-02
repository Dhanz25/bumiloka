package com.faiz.bumiloka

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class TantanganDiriFragment : Fragment(R.layout.fragment_tantangan_diri) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnMulaiKuis = view.findViewById<MaterialButton>(R.id.btnMulaiKuis)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // ✅ Ambil level aktif
        val currentLevel = LevelHelper.getCurrentLevelLocal(requireContext())

            val prefMisi = requireActivity().getSharedPreferences(
                "MISI_${userId}_LEVEL_$currentLevel",
                Context.MODE_PRIVATE
            )

            val sudahSelesai = prefMisi.getBoolean("misi2_selesai", false)

            // ===============================
            // STATUS BUTTON
            // ===============================
            if (sudahSelesai) {

                btnMulaiKuis.text = "Selesai ✓"
                btnMulaiKuis.isEnabled = false

            } else {

                btnMulaiKuis.text = "Mulai Tantangan"
                btnMulaiKuis.isEnabled = true

                btnMulaiKuis.setOnClickListener {

                    val fragment = QuizSoalFragment()

                    fragment.arguments = Bundle().apply {
                        putString("FROM", "MISI")
                    }

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }