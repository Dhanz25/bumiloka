package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat

class QuizMenang1Fragment : Fragment(R.layout.fragment_quiz_menang1_) {

    private var isPointAwarded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvBenar = view.findViewById<TextView>(R.id.tvBenar)
        val tvSalah = view.findViewById<TextView>(R.id.tvSalah)
        val layoutSkorSempurna = view.findViewById<LinearLayout>(R.id.layoutSkorSempurna)
        val tvSkorBiasa = view.findViewById<TextView>(R.id.tvSkorBiasa)
        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)
        
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        val btnUlangi = view.findViewById<Button>(R.id.btnUlangi)
        val btnTips = view.findViewById<Button>(R.id.btnTips)

        val args = arguments ?: Bundle()
        val kuisId = args.getString("KUIS_ID") ?: ""
        val level = args.getInt("LEVEL", 1)
        val isTantanganBonus = args.getBoolean("IS_TANTANGAN_BONUS", false)
        
        val quizIdStr = args.getString("quiz_id") ?: kuisId
        val challengeId = args.getString("challenge_id") ?: ""
        val badgeId = args.getString("badge_id") ?: ""
        val materiId = args.getString("materi_id") ?: ""
        
        val skor = args.getInt("SKOR", 0)
        val benar = args.getInt("BENAR", 0)
        val salah = args.getInt("SALAH", 0)
        val poinReward = args.getInt("POIN_REWARD", 20)

        tvBenar.text = benar.toString()
        tvSalah.text = salah.toString()

        if (skor >= 100) {
            layoutSkorSempurna.visibility = View.VISIBLE
            tvSkorBiasa.visibility = View.GONE
            tvGreeting.text = "Luar Biasa!"
        } else {
            layoutSkorSempurna.visibility = View.GONE
            tvSkorBiasa.visibility = View.VISIBLE
            tvSkorBiasa.text = "$skor/100"
            tvGreeting.text = if (skor >= 75) "Bagus Sekali!" else "Tetap Semangat!"
        }

        TantanganStatusHelper.syncAllProgress(requireContext(), level, "QUIZ", quizIdStr, skor)
        
        // CEK AGAR TIDAK DOUBLE POINT
        if (!isPointAwarded && savedInstanceState == null) {
            if (skor >= 75 && !isTantanganBonus) {
                AktivitasHelper.tambahPoint(requireContext(), poinReward, "Kuis")
                isPointAwarded = true
            }
        }

        if (skor >= 75) {
            btnTips.visibility = View.VISIBLE
            btnTips.setOnClickListener {
                val fragmentTips: Fragment = when (level) {
                    1 -> TipsPeduliFragment()
                    2 -> TipsSampahFragment()
                    3 -> TipsHematAirFragment()
                    else -> TipsFragment()
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragmentTips)
                    .addToBackStack(null).commit()
            }
        }

        if (challengeId.isNotEmpty() && skor >= 75) {
            if (!isTantanganBonus) {
                TantanganStatusHelper.setTantanganSelesai(requireContext(), challengeId, materiId, quizIdStr, skor)
                if (badgeId.isNotEmpty()) {
                    // SEKARANG MUNCULKAN POPUP LENCANA
                    BadgeHelper.tambahBadge(requireContext(), badgeId, true)
                }
            }
        }

        if (skor > 0) showNotifMisiSelesai()

        btnOk.setOnClickListener { parentFragmentManager.popBackStack() }

        btnUlangi.setOnClickListener {
            val fragment = when (args.getString("QUIZ_TYPE")) {
                "QUIZ2" -> QuizSoal2Fragment.newInstance(level)
                "QUIZ3" -> QuizSoal3Fragment.newInstance(level)
                else -> QuizSoalFragment.newInstance(quizIdStr, level)
            }
            fragment.arguments = (fragment.arguments ?: Bundle()).apply { putAll(args) }
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
        }

        view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun showNotifMisiSelesai() {
        if (!isAdded) return
        try {
            val notifView = layoutInflater.inflate(R.layout.notif_misi_selesai, null)
            val notifDialog = AlertDialog.Builder(requireContext()).setView(notifView).create()
            notifDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            notifDialog.show()
            notifView.postDelayed({ if (notifDialog.isShowing) notifDialog.dismiss() }, 2000)
        } catch (e: Exception) {}
    }
}
