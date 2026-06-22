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

        val args = arguments ?: Bundle()
        val quizType = args.getString("QUIZ_TYPE") ?: "QUIZ1"
        val dariMisi = args.getBoolean("DARI_MISI", false) ?: false
        val isTantanganBonus = args.getBoolean("IS_TANTANGAN_BONUS", false) ?: false
        val badgeId = args.getInt("badge_id", 0)
        val challengeId = args.getString("challenge_id") ?: ""
        val quizId = args.getInt("quiz_id", 1)
        val materiId = args.getInt("materi_id", 1)

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val pref = requireActivity().getSharedPreferences("KUIS_$userId", Context.MODE_PRIVATE)

        val skorBundle = args.getInt("SKOR", -1)
        val benarBundle = args.getInt("BENAR", -1)
        val salahBundle = args.getInt("SALAH", -1)

        val skor: Int
        val benar: Int
        val salah: Int

        if (skorBundle != -1) {
            skor = skorBundle
            benar = benarBundle
            salah = salahBundle
        } else {
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

        // LOGIKA REWARD & PROGRESS
        if (isTantanganBonus) {
            if (skor >= 75) {
                // 1. Berikan Badge
                if (badgeId != 0) {
                    BadgeHelper.tambahBadge(requireContext(), badgeId.toString())
                    AktivitasHelper.tambahLencana(badgeId.toString()) // Simpan ke Firebase
                }
                
                // 2. Set Status Selesai
                if (challengeId.isNotEmpty()) {
                    TantanganStatusHelper.setTantanganBonusSelesai(requireContext(), challengeId, materiId, quizId, skor)
                }
            }
        } else {
            // PROGRESS LEVEL/MISI UTAMA
            LevelHelper.getCurrentLevel(requireContext()) { currentLevel ->
                val prefMisi = requireActivity().getSharedPreferences("MISI_${userId}_LEVEL_$currentLevel", Context.MODE_PRIVATE)

                if (quizType == "QUIZ1" && skor > 0) {
                    if (!prefMisi.getBoolean("misi2_selesai", false)) {
                        prefMisi.edit().putBoolean("misi2_selesai", true).apply()
                        AktivitasHelper.tambahPoint(requireContext(), 30)
                        AktivitasHelper.tambahMisiSelesai()
                        AktivitasManager.tambahAktivitas(requireContext(), "Berhasil menyelesaikan Misi Tantangan Diri", "Misi", 30)
                        showNotifMisiSelesai()
                        UnlockLevelHelper.checkAndUnlockNextLevel(requireContext(), currentLevel)
                    }
                }

                if (quizType == "QUIZ3" && skor >= 75) {
                    if (!prefMisi.getBoolean("misi3_selesai", false)) {
                        prefMisi.edit().putBoolean("misi3_selesai", true).apply()
                        AktivitasHelper.tambahPoint(requireContext(), 40)
                        AktivitasHelper.tambahMisiSelesai()
                        AktivitasManager.tambahAktivitas(requireContext(), "Berhasil menyelesaikan Misi Raih Skor 75", "Misi", 40)
                        showNotifMisiSelesai()
                        UnlockLevelHelper.checkAndUnlockNextLevel(requireContext(), currentLevel)
                    }
                }
            }
        }

        btnUlangi.visibility = if (skor == 100) View.GONE else View.VISIBLE

        btnOk.setOnClickListener {
            if (isTantanganBonus) {
                if (skor >= 75) {
                    Toast.makeText(requireContext(), "Selamat! Badge berhasil didapatkan.", Toast.LENGTH_SHORT).show()
                }
                parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TantanganFragment())
                    .commit()
            } else if (quizType == "QUIZ3" && dariMisi && skor < 75) {
                showBelumRaihSkorPopup()
            } else if (dariMisi) {
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, MisiFragment()).commit()
            } else {
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, QuizUtamaFragment()).commit()
            }
        }

        btnUlangi.setOnClickListener {
            val fragment = when (quizType) {
                "QUIZ2" -> QuizSoal2Fragment()
                "QUIZ3" -> QuizSoal3Fragment()
                else -> QuizSoalFragment()
            }
            fragment.arguments = Bundle().apply { putAll(args) }
            showUlangiDialog(fragment)
        }

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun showNotifMisiSelesai() {
        if (!isAdded) return
        val notifView = layoutInflater.inflate(R.layout.notif_misi_selesai, null)
        val notifDialog = AlertDialog.Builder(requireContext()).setView(notifView).create()
        notifDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        notifDialog.show()
        notifDialog.window?.setGravity(android.view.Gravity.TOP or android.view.Gravity.START)
        notifDialog.window?.attributes = notifDialog.window?.attributes?.apply { x = 30; y = 120 }
        notifView.postDelayed({ if (notifDialog.isShowing) notifDialog.dismiss() }, 2000)
    }

    private fun showBelumRaihSkorPopup() {
        if (!isAdded) return
        val dialogView = layoutInflater.inflate(R.layout.popup_belumraihskor, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(false).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        dialogView.findViewById<Button>(R.id.btnLanjut).setOnClickListener {
            dialog.dismiss()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, MisiFragment()).commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
    }

    private fun showUlangiDialog(fragment: Fragment) {
        if (!isAdded) return
        val dialogView = layoutInflater.inflate(R.layout.popup_kerjakanulang, null)
        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val btnYa = dialogView.findViewById<Button>(R.id.btnYa)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(false).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        btnBatal.setOnClickListener { dialog.dismiss() }
        btnYa.setOnClickListener {
            dialog.dismiss()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
        }
        dialog.show()
    }
}