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
        val kuisId = args.getString("KUIS_ID")
        val quizType = args.getString("QUIZ_TYPE") ?: "QUIZ_DYNAMIC"
        val level = args.getInt("LEVEL", 1)
        val dariMisi = args.getBoolean("DARI_MISI", false)
        val isTantanganBonus = args.getBoolean("IS_TANTANGAN_BONUS", false)
        
        // Metadata Tantangan (String support)
        val badgeId = args.getString("badge_id") ?: ""
        val challengeId = args.getString("challenge_id") ?: ""
        val quizId = args.getString("quiz_id") ?: ""
        val materiId = args.getString("materi_id") ?: ""

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        
        val prefKuis = requireActivity().getSharedPreferences("KUIS_${userId}_LEVEL_$level", Context.MODE_PRIVATE)

        val skor = args.getInt("SKOR", 0)
        val benar = args.getInt("BENAR", 0)
        val salah = args.getInt("SALAH", 0)

        tvBenar.text = "$benar"
        tvSalah.text = "$salah"
        tvSkor.text = "Skor: $skor/100"

        // SIMPAN STATUS KUIS SELESAI
        if (challengeId.isEmpty()) {
            val editor = prefKuis.edit()
            if (!kuisId.isNullOrEmpty()) {
                editor.putBoolean("kuis_${kuisId}_selesai", true)
                editor.putInt("kuis_${kuisId}_skor", skor)
            }
            
            when (quizType) {
                "QUIZ1" -> {
                    editor.putBoolean("materi1_selesai", true)
                    editor.putInt("nilai_materi1", skor)
                }
                "QUIZ2" -> {
                    editor.putBoolean("quiz2_selesai", true)
                    editor.putInt("quiz2_nilai", skor)
                }
                "QUIZ3" -> {
                    editor.putBoolean("quiz3_selesai", true)
                    editor.putInt("quiz3_nilai", skor)
                }
            }
            editor.apply()
        }

        // LOGIKA PENYELESAIAN TANTANGAN UMUM / DINAMIS
        if (challengeId.isNotEmpty() && skor >= 75) {
            TantanganStatusHelper.setTantanganSelesai(requireContext(), challengeId, materiId, quizId, skor)
            
            if (badgeId.isNotEmpty()) {
                BadgeHelper.tambahBadge(requireContext(), badgeId)
                AktivitasManager.tambahAktivitas(requireContext(), "Mendapatkan Lencana baru dari Tantangan", "Lencana", 50)
                AktivitasHelper.tambahPoint(requireContext(), 50, "Tantangan")
            }
        }

        // LOGIKA MISI (Hanya untuk kuis utama)
        if (!isTantanganBonus && challengeId.isEmpty()) {
            LevelHelper.getCurrentLevel(requireContext()) { currentLevel ->
                val prefMisi = requireActivity().getSharedPreferences("MISI_${userId}_LEVEL_$currentLevel", Context.MODE_PRIVATE)

                if ((quizType == "QUIZ1" || !kuisId.isNullOrEmpty()) && skor > 0) {
                    if (!prefMisi.getBoolean("misi2_selesai", false)) {
                        prefMisi.edit().putBoolean("misi2_selesai", true).apply()
                        AktivitasHelper.tambahPoint(requireContext(), 30, "Penyelesaian Kuis")
                        AktivitasHelper.tambahMisiSelesai(requireContext(), showNotification = false)
                        showNotifMisiSelesai()
                        UnlockLevelHelper.checkAndUnlockNextLevel(requireContext(), currentLevel)
                    }
                }

                if ((quizType == "QUIZ3" || !kuisId.isNullOrEmpty()) && skor >= 75) {
                    if (!prefMisi.getBoolean("misi3_selesai", false)) {
                        prefMisi.edit().putBoolean("misi3_selesai", true).apply()
                        AktivitasHelper.tambahPoint(requireContext(), 40, "Skor Tinggi Kuis")
                        AktivitasHelper.tambahMisiSelesai(requireContext(), showNotification = false)
                        showNotifMisiSelesai()
                        UnlockLevelHelper.checkAndUnlockNextLevel(requireContext(), currentLevel)
                    }
                }
            }
        }

        btnUlangi.visibility = if (skor == 100) View.GONE else View.VISIBLE

        btnOk.setOnClickListener {
            if (challengeId.isNotEmpty()) {
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
            val fragment = if (kuisId != null) {
                QuizSoalFragment.newInstance(kuisId, level)
            } else {
                when (quizType) {
                    "QUIZ2" -> QuizSoal2Fragment()
                    "QUIZ3" -> QuizSoal3Fragment()
                    else -> QuizSoalFragment()
                }
            }
            fragment.arguments = Bundle().apply { 
                putAll(args) 
                if (kuisId != null) putString("KUIS_ID", kuisId)
            }
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