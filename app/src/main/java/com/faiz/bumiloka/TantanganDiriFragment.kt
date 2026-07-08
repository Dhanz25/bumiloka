package com.faiz.bumiloka

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class TantanganDiriFragment : Fragment(R.layout.fragment_tantangan_diri) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnMulaiKuis = view.findViewById<MaterialButton>(R.id.btnMulaiKuis)
        val tvTitle = view.findViewById<TextView>(R.id.tvTantanganDiriTitle)
        val tvDesc = view.findViewById<TextView>(R.id.tvTantanganDiriDesc)
        val ivIcon = view.findViewById<ImageView>(R.id.ivMisiIcon)

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        val ctx = context ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

        LevelHelper.getCurrentLevel(ctx) { currentLevel ->
            if (!isAdded) return@getCurrentLevel

            val prefMisi = requireActivity().getSharedPreferences("MISI_${userId}_LEVEL_$currentLevel", Context.MODE_PRIVATE)
            val sudahSelesai = prefMisi.getBoolean("misi2_selesai", false)

            tvTitle.text = "Selesaikan Kuis Level $currentLevel"
            tvDesc.text = "Uji pemahamanmu tentang materi di Level $currentLevel. Kamu harus mendapatkan skor untuk membuka misi berikutnya."
            
            when(currentLevel) {
                1 -> ivIcon.setImageResource(R.drawable.img_lingkungan)
                2 -> ivIcon.setImageResource(R.drawable.img_sampah)
                3 -> ivIcon.setImageResource(R.drawable.img_air)
                else -> ivIcon.setImageResource(R.drawable.img_lingkungan)
            }

            if (sudahSelesai) {
                btnMulaiKuis.text = "Selesai ✓"
                btnMulaiKuis.isEnabled = false
            } else {
                btnMulaiKuis.text = "Mulai Tantangan"
                btnMulaiKuis.isEnabled = true
                btnMulaiKuis.setOnClickListener {
                    if (!isAdded) return@setOnClickListener
                    val targetKuisId = (3 * (currentLevel - 1) + 1).toString()
                    val fragment = QuizSoalFragment.newInstance(targetKuisId, currentLevel)
                    fragment.arguments = (fragment.arguments ?: Bundle()).apply {
                        putBoolean("DARI_MISI", true)
                        putString("QUIZ_TYPE", "QUIZ_DYNAMIC")
                    }
                    parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
                }
            }
        }
    }
}
