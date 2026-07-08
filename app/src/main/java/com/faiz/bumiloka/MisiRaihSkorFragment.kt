package com.faiz.bumiloka

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class MisiRaihSkorFragment : Fragment(R.layout.fragment_misi_raih_skor) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnMulaiKuis = view.findViewById<MaterialButton>(R.id.btnMulaiKuis)
        val tvDesc = view.findViewById<TextView>(R.id.tvMisiRaihSkorDesc)
        val ivIcon = view.findViewById<ImageView>(R.id.ivMisiIcon)

        btnBack.setOnClickListener { if (isAdded) parentFragmentManager.popBackStack() }

        val ctx = context ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

        LevelHelper.getCurrentLevel(ctx) { currentLevel ->
            if (!isAdded) return@getCurrentLevel

            val prefKuis = requireActivity().getSharedPreferences("KUIS_${userId}_LEVEL_$currentLevel", Context.MODE_PRIVATE)
            
            val temaMisi = when(currentLevel) {
                1 -> "Hemat Air"
                2 -> "Kelola Ekosistem"
                3 -> "Pelestarian Alam"
                else -> "Lingkungan"
            }
            
            tvDesc?.text = "Dapatkan skor minimal 75 pada Kuis 3 ($temaMisi) di Level $currentLevel untuk menyelesaikan misi ini."
            
            when(currentLevel) {
                1 -> ivIcon?.setImageResource(R.drawable.img_air)
                2 -> ivIcon?.setImageResource(R.drawable.img_sampah)
                3 -> ivIcon?.setImageResource(R.drawable.img_lingkungan)
            }

            btnMulaiKuis.setOnClickListener {
                if (!isAdded) return@setOnClickListener
                val currentNilai = prefKuis.getInt("quiz3_nilai", 0)

                if (currentNilai >= 75) {
                    TantanganStatusHelper.syncAllProgress(context, currentLevel, "QUIZ", "3", currentNilai)
                    AktivitasManager.tambahAktivitas(requireContext(), "Menyelesaikan Misi Skor Tinggi Level $currentLevel", "Misi", 40)
                    showSuccessPopup()
                } else {
                    val targetKuisId = (3 * (currentLevel - 1) + 3).toString()
                    val fragment = when(currentLevel) {
                        1 -> QuizSoal3Fragment.newInstance(currentLevel)
                        else -> QuizSoalFragment.newInstance(targetKuisId, currentLevel)
                    }

                    fragment.arguments = (fragment.arguments ?: Bundle()).apply {
                        putBoolean("DARI_MISI", true)
                        putInt("LEVEL", currentLevel)
                        putString("QUIZ_TYPE", if (currentLevel == 1) "QUIZ3" else "QUIZ_DYNAMIC")
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    private fun showSuccessPopup() {
        if (!isAdded) return
        val dialogView = layoutInflater.inflate(R.layout.popup_raihskor, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView).setCancelable(false).create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        dialogView.findViewById<Button>(R.id.btnLanjut).setOnClickListener {
            dialog.dismiss()
            if (isAdded) parentFragmentManager.popBackStack()
        }
    }
}
