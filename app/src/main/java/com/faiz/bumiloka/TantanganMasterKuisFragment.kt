package com.faiz.bumiloka

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class TantanganMasterKuisFragment :
    Fragment(R.layout.fragment_tantangan_master_kuis) {

    private lateinit var tvProgressUtama: TextView
    private lateinit var progressBarUtama: ProgressBar

    private lateinit var tvProgressKuis: TextView
    private lateinit var progressBarKuis: ProgressBar

    private lateinit var tvProgressNilai75: TextView
    private lateinit var progressBarNilai75: ProgressBar

    private lateinit var btnMulaiMateri: Button
    private lateinit var btnMulaiKuis: Button
    private lateinit var btnSelesai: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<View>(R.id.bottom_navigation)
            .visibility = View.GONE

        val btnBack = view.findViewById<View>(R.id.btnBack)

        tvProgressUtama = view.findViewById(R.id.tvProgressUtama)
        progressBarUtama = view.findViewById(R.id.progressBarUtama)

        tvProgressKuis = view.findViewById(R.id.tvProgressKuis)
        progressBarKuis = view.findViewById(R.id.progressBarKuis)

        tvProgressNilai75 = view.findViewById(R.id.tvProgressNilai75)
        progressBarNilai75 = view.findViewById(R.id.progressBarNilai75)

        btnMulaiMateri = view.findViewById(R.id.btnMulaiMateri)
        btnMulaiKuis = view.findViewById(R.id.btnMulaiKuis)
        btnSelesai = view.findViewById(R.id.btnSelesai)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnMulaiMateri.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QuizUtamaFragment())
                .addToBackStack(null)
                .commit()
        }

        btnMulaiKuis.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QuizUtamaFragment())
                .addToBackStack(null)
                .commit()
        }

        btnSelesai.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        updateProgress()
    }

    override fun onResume() {
        super.onResume()
        updateProgress()
    }

    private fun updateProgress() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

        LevelHelper.getCurrentLevel(requireContext()) { level ->

            val pref = requireActivity()
                .getSharedPreferences(
                    "KUIS_${userId}_LEVEL_$level",
                    Context.MODE_PRIVATE
                )

            // =====================
            // MENYELESAIKAN KUIS
            // =====================

            var kuisSelesai = 0

            if (pref.getBoolean("materi1_selesai", false))
                kuisSelesai++

            if (pref.getBoolean("quiz2_selesai", false))
                kuisSelesai++

            if (pref.getBoolean("quiz3_selesai", false))
                kuisSelesai++

            tvProgressKuis.text =
                "$kuisSelesai/3 Progress selesai"

            progressBarKuis.progress =
                (kuisSelesai * 100) / 3


            // =====================
            // SKOR >= 75
            // =====================

            var skor75 = 0

            if (pref.getInt("nilai_materi1", 0) >= 75)
                skor75++

            if (pref.getInt("quiz2_nilai", 0) >= 75)
                skor75++

            if (pref.getInt("quiz3_nilai", 0) >= 75)
                skor75++

            tvProgressNilai75.text =
                "$skor75/3 Progress selesai"

            progressBarNilai75.progress =
                (skor75 * 100) / 3


            // =====================
            // PROGRESS UTAMA
            // =====================

            val totalProgress =
                kuisSelesai + skor75

            val persen =
                (totalProgress * 100) / 6

            tvProgressUtama.text =
                "Progress $persen%"

            progressBarUtama.progress = persen


            // =====================
            // TOMBOL
            // =====================

            if (kuisSelesai == 3) {
                btnMulaiMateri.text = "Selesai ✓"
                btnMulaiMateri.isEnabled = false
            }

            if (skor75 == 3) {
                btnMulaiKuis.text = "Selesai ✓"
                btnMulaiKuis.isEnabled = false
            }

            btnSelesai.isEnabled =
                (kuisSelesai == 3 && skor75 == 3)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        requireActivity()
            .findViewById<View>(R.id.bottom_navigation)
            .visibility = View.VISIBLE
    }
}