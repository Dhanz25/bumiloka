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
        val dariMisi = arguments?.getBoolean("DARI_MISI", false) ?: false

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


        // ✅ SharedPreferences misi
        LevelHelper.getCurrentLevel(requireContext()) { currentLevel ->

            val prefMisi = requireActivity()
                .getSharedPreferences(
                    "MISI_${userId}_LEVEL_$currentLevel",
                    Context.MODE_PRIVATE
                )

            // ===============================
            // MISI 2 → Tantangan Diri
            // ===============================
            if (quizType == "QUIZ1" && skor > 0) {

                val sudahMisi2 = prefMisi.getBoolean("misi2_selesai", false)

                if (!sudahMisi2) {

                    prefMisi.edit()
                        .putBoolean("misi2_selesai", true)
                        .apply()

                    AktivitasHelper.tambahPoint(requireContext(), 30)
                    AktivitasHelper.tambahMisiSelesai()

                    AktivitasManager.tambahAktivitas(
                        requireContext(),
                        "Berhasil menyelesaikan Misi Tantangan Diri",
                        "Misi",
                        30
                    )

                    val notifView = layoutInflater.inflate(
                        R.layout.notif_misi_selesai,
                        null
                    )

                    val notifDialog = AlertDialog.Builder(requireContext())
                        .setView(notifView)
                        .create()

                    notifDialog.window?.setBackgroundDrawableResource(
                        android.R.color.transparent
                    )

                    notifDialog.show()

                    notifDialog.window?.setGravity(
                        android.view.Gravity.TOP or android.view.Gravity.START
                    )

                    notifDialog.window?.attributes =
                        notifDialog.window?.attributes?.apply {
                            x = 30
                            y = 120
                        }

                    notifView.postDelayed({
                        notifDialog.dismiss()
                    }, 2000)

                    UnlockLevelHelper.checkAndUnlockNextLevel(
                        requireContext(),
                        currentLevel
                    )
                }
            }

            // ===============================
            // MISI 3 → Raih Skor 75
            // ===============================
            if (quizType == "QUIZ3" && skor >= 75) {

                val sudahMisi3 = prefMisi.getBoolean("misi3_selesai", false)

                if (!sudahMisi3) {

                    prefMisi.edit()
                        .putBoolean("misi3_selesai", true)
                        .apply()

                    AktivitasHelper.tambahPoint(requireContext(), 40)
                    AktivitasHelper.tambahMisiSelesai()

                    AktivitasManager.tambahAktivitas(
                        requireContext(),
                        "Berhasil menyelesaikan Misi Raih Skor 75",
                        "Misi",
                        40
                    )

                    val notifView = layoutInflater.inflate(
                        R.layout.notif_misi_selesai,
                        null
                    )

                    val notifDialog = AlertDialog.Builder(requireContext())
                        .setView(notifView)
                        .create()

                    notifDialog.window?.setBackgroundDrawableResource(
                        android.R.color.transparent
                    )

                    notifDialog.show()

                    notifDialog.window?.setGravity(
                        android.view.Gravity.TOP or android.view.Gravity.START
                    )

                    notifDialog.window?.attributes =
                        notifDialog.window?.attributes?.apply {
                            x = 30
                            y = 120
                        }

                    notifView.postDelayed({
                        notifDialog.dismiss()
                    }, 2000)

                    UnlockLevelHelper.checkAndUnlockNextLevel(
                        requireContext(),
                        currentLevel
                    )
                }
            }
        }

        // ✅ ULANGI
        if (skor == 100) {
            btnUlangi.visibility = View.GONE
        } else {
            btnUlangi.visibility = View.VISIBLE
        }
        val finalSkor = skor

        val dariTantangan = arguments?.getBoolean("DARI_TANTANGAN", false) ?: false

        btnOk.setOnClickListener {
            if (quizType == "QUIZ3" && dariMisi && finalSkor < 75) {
                val dialogView = layoutInflater.inflate(R.layout.popup_belumraihskor, null)
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create()
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()
                val btnLanjut = dialogView.findViewById<Button>(R.id.btnLanjut)
                btnLanjut.setOnClickListener {
                    dialog.dismiss()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MisiFragment())
                        .commit()
                }
            } else if (dariMisi) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MisiFragment())
                    .commit()
            } else if (dariTantangan) {
                // Hapus semua back stack sampai ketemu TantanganPenjelajahMingguanFragment
                parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TantanganPenjelajahMingguanFragment())
                    .commit()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, QuizUtamaFragment())
                    .commit()
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.VISIBLE
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