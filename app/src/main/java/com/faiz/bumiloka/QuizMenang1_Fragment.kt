package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import android.content.Context
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth

class QuizMenang1Fragment : Fragment(R.layout.fragment_quiz_menang1_) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvBenar = view.findViewById<TextView>(R.id.tvBenar)
        val tvSalah = view.findViewById<TextView>(R.id.tvSalah)
        val tvSkor = view.findViewById<TextView>(R.id.tvSkor)
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        val btnUlangi = view.findViewById<Button>(R.id.btnUlangi)

        val quizType = arguments?.getString("QUIZ_TYPE") ?: "QUIZ1"

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val pref = requireActivity().getSharedPreferences("KUIS_$userId", Context.MODE_PRIVATE)

        // ✅ Ambil dari Bundle dulu (REAL TIME dari soal)
        val skorBundle = arguments?.getInt("SKOR", -1) ?: -1
        val benarBundle = arguments?.getInt("BENAR", -1) ?: -1
        val salahBundle = arguments?.getInt("SALAH", -1) ?: -1

        val skor: Int
        val benar: Int
        val salah: Int

        if (skorBundle != -1) {
            // ✅ kalau dari soal (langsung setelah selesai)
            skor = skorBundle
            benar = benarBundle
            salah = salahBundle
        } else {
            // ✅ fallback kalau buka dari "Lihat Hasil"
            skor = when (quizType) {
                "QUIZ1" -> pref.getInt("nilai_materi1", 0)
                "QUIZ2" -> pref.getInt("quiz2_nilai", 0)
                "QUIZ3" -> pref.getInt("quiz3_nilai", 0)
                else -> 0
            }

            benar = skor / 10
            salah = 10 - benar
        }

        tvBenar.text = "$benar"
        tvSalah.text = "$salah"
        tvSkor.text = "Skor: $skor/100"

        // ✅ ULANGI
        if (skor == 100) {
            btnUlangi.visibility = View.GONE
        } else {
            btnUlangi.visibility = View.VISIBLE
        }

        // ✅ OK → balik ke menu kuis
        btnOk.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QuizUtamaFragment())
                .commit()
        }

        // ✅ ULANGI SESUAI QUIZ
        btnUlangi.setOnClickListener {

            val fragment = when (quizType) {
                "QUIZ1" -> QuizSoalFragment()
                "QUIZ2" -> QuizSoal2Fragment()
                "QUIZ3" -> QuizSoal3Fragment()
                else -> QuizSoalFragment()
            }
            showUlangiDialog(fragment)
        }



        toolbar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun showUlangiDialog(fragment: Fragment) {
        val dialogView = layoutInflater.inflate(R.layout.popup_kerjakanulang, null)

        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val btnYa = dialogView.findViewById<Button>(R.id.btnYa)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnYa.setOnClickListener {
            dialog.dismiss()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        dialog.show()
        }
}