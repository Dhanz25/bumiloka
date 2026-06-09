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

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        LevelHelper.getCurrentLevel(requireContext()) { currentLevel ->
            val prefKuis = requireActivity().getSharedPreferences("KUIS_${userId}_LEVEL_$currentLevel", Context.MODE_PRIVATE)
            val prefMisi = requireActivity().getSharedPreferences("MISI_${userId}_LEVEL_$currentLevel", Context.MODE_PRIVATE)

            val nilai = prefKuis.getInt("quiz3_nilai", 0)
            
            // Update UI deskripsi sesuai level
            tvDesc?.text = "Dapatkan skor minimal 75 pada Kuis 3 di Level $currentLevel untuk menyelesaikan misi ini."

            // ================= LOGIKA =================
            btnMulaiKuis.setOnClickListener {

                val currentNilai = prefKuis.getInt("quiz3_nilai", 0)

                if (currentNilai >= 75) {

                    // ✅ TAMBAH DI SINI (MASUK KE RIWAYAT)
                    AktivitasManager.tambahAktivitas(
                        requireContext(),
                        "Menyelesaikan Misi Raih Skor Level $currentLevel",
                        "Misi",
                        20
                    )

                    // simpan status misi selesai
                    prefMisi.edit().putBoolean("misi3_selesai", true).apply()

                    // 🔥 tampilkan popup
                    val viewDialog = layoutInflater.inflate(R.layout.popup_raihskor, null)

                    val dialog = AlertDialog.Builder(requireContext())
                        .setView(viewDialog)
                        .setCancelable(false)
                        .create()

                    dialog.show()

                    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    dialog.window?.setDimAmount(0.6f)

                    val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_up_scale)
                    viewDialog.startAnimation(anim)

                    val btnLanjut = viewDialog.findViewById<Button>(R.id.btnLanjut)

                    btnLanjut.setOnClickListener {
                        dialog.dismiss()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, MisiFragment())
                            .commit()
                    }

                } else {

                    val fragment = QuizSoal3Fragment()

                    fragment.arguments = Bundle().apply {
                        putBoolean("DARI_MISI", true)
                        putInt("LEVEL", currentLevel)
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }
}
