// ===============================
// FILE: DetailTantanganFragment.kt
// SESUAI DENGAN layout fragment_detail_tantangan.xml
// TANPA FIREBASE (SharedPreferences)
// ===============================

package com.faiz.bumiloka

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.data.ChallengeManager

class DetailTantanganFragment : Fragment(R.layout.fragment_detailtantangan) {

    private lateinit var btnBack: ImageButton
    private lateinit var btnMulaiMateri: Button
    private lateinit var btnMulaiKuis: Button
    private lateinit var btnSelesai: Button

    private lateinit var tvProgressUtama: TextView
    private lateinit var progressBarUtama: ProgressBar

    private lateinit var tvProgressMateri: TextView
    private lateinit var progressBarMateri: ProgressBar

    private lateinit var tvProgressKuis: TextView
    private lateinit var progressBarKuis: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // =========================
        // HUBUNGKAN ID XML
        // =========================
        btnBack = view.findViewById(R.id.btnBack)
        btnMulaiMateri = view.findViewById(R.id.btnMulaiMateri)
        btnMulaiKuis = view.findViewById(R.id.btnMulaiKuis)
        btnSelesai = view.findViewById(R.id.btnSelesai)

        tvProgressUtama = view.findViewById(R.id.tvProgressUtama)
        progressBarUtama = view.findViewById(R.id.progressBarUtama)

        tvProgressMateri = view.findViewById(R.id.tvProgressMateri)
        progressBarMateri = view.findViewById(R.id.progressBarMateri)

        tvProgressKuis = view.findViewById(R.id.tvProgressKuis)
        progressBarKuis = view.findViewById(R.id.progressBarKuis)

        // =========================
        // LOAD PROGRESS SAAT MASUK
        // =========================
        loadProgress()

        // =========================
        // BACK
        // =========================
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // =========================
        // TOMBOL MULAI MATERI
        // =========================
        btnMulaiMateri.setOnClickListener {

            val result = ChallengeManager.updateProgressMateri(requireContext())

            Toast.makeText(
                requireContext(),
                "Progress Materi: ${result.first}/${result.second}",
                Toast.LENGTH_SHORT
            ).show()

            loadProgress()

            // OPTIONAL:
            // pindah ke halaman materi
            // startActivity(Intent(requireContext(), Jelajahi_MateriDetail::class.java))
        }

        // =========================
        // TOMBOL MULAI KUIS
        // =========================
        btnMulaiKuis.setOnClickListener {

            val challenge = ChallengeManager.loadChallenge(requireContext())

            // Kuis hanya bisa jika materi 3/3
            if (challenge.materiSelesai < challenge.totalMateri) {

                Toast.makeText(
                    requireContext(),
                    "Selesaikan semua materi dulu!",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                ChallengeManager.updateProgressKuis(requireContext())

                Toast.makeText(
                    requireContext(),
                    "Kuis selesai!",
                    Toast.LENGTH_SHORT
                ).show()

                loadProgress()

                // OPTIONAL:
                // pindah ke fragment kuis
            }
        }

        // =========================
        // TOMBOL SELESAI
        // =========================
        btnSelesai.setOnClickListener {

            val challenge = ChallengeManager.loadChallenge(requireContext())

            if (challenge.progress == 100) {

                Toast.makeText(
                    requireContext(),
                    "Tantangan berhasil diselesaikan!",
                    Toast.LENGTH_LONG
                ).show()

                parentFragmentManager.popBackStack()

            } else {

                Toast.makeText(
                    requireContext(),
                    "Progress belum 100%",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // =========================
    // LOAD UI PROGRESS
    // =========================
    private fun loadProgress() {

        val challenge = ChallengeManager.loadChallenge(requireContext())

        // =====================
        // PROGRESS MATERI
        // =====================
        tvProgressMateri.text =
            "${challenge.materiSelesai}/${challenge.totalMateri} Progress selesai"

        val progressMateri =
            ((challenge.materiSelesai.toDouble() / challenge.totalMateri) * 100).toInt()

        progressBarMateri.progress = progressMateri

        // =====================
        // PROGRESS KUIS
        // =====================
        if (challenge.status == "selesai") {
            tvProgressKuis.text = "Kuis selesai"
            progressBarKuis.progress = 100
        } else {
            tvProgressKuis.text = "Belum selesai"
            progressBarKuis.progress = 0
        }

        // =====================
        // PROGRESS UTAMA
        // =====================
        tvProgressUtama.text = "Progress ${challenge.progress}%"
        progressBarUtama.progress = challenge.progress

        // =====================
        // BUTTON STATUS
        // =====================
        if (challenge.materiSelesai == challenge.totalMateri) {
            btnMulaiMateri.text = "Selesai"
        }

        if (challenge.status == "selesai") {
            btnMulaiKuis.text = "Selesai"
            btnSelesai.text = "Tantangan Selesai"
        }

        // =====================
        // DEADLINE CEK
        // =====================
        if (ChallengeManager.isChallengeExpired(requireContext())
            && challenge.status != "selesai"
        ) {

            btnSelesai.text = "Tantangan Gagal"
            btnMulaiMateri.isEnabled = false
            btnMulaiKuis.isEnabled = false
        }
    }
}