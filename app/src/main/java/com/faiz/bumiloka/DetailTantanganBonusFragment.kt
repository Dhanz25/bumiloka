package com.faiz.bumiloka

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.model.BonusChallengeModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class DetailTantanganBonusFragment : Fragment(R.layout.fragment_detail_tantangan_bonus) {

    private var bonusId: String? = null
    private var judul: String? = null
    private var deskripsi: String? = null
    private var badgeId: String? = null
    private var type: String = "COMMITMENT"
    private var targetDays: Int = 1
    private var quizId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

        arguments?.let {
            bonusId = it.getString("id") ?: it.getString("challenge_id")
            judul = it.getString("judul")
            deskripsi = it.getString("deskripsi")
            badgeId = it.getString("badgeId") ?: it.getString("badge_id")
            type = it.getString("type", "COMMITMENT")
            targetDays = it.getInt("targetDays", 1)
            quizId = it.getString("quizId") ?: it.getString("quiz_id")
        }

        val tvJudul = view.findViewById<TextView>(R.id.tvJudulBonus)
        val tvDesc = view.findViewById<TextView>(R.id.tvDescBonus)
        val tvProgress = view.findViewById<TextView>(R.id.tvProgressBonus)
        val btnAksi = view.findViewById<Button>(R.id.btnAksiBonus)
        val ivIcon = view.findViewById<ImageView>(R.id.ivIconType)

        tvJudul.text = judul ?: ""
        tvDesc.text = deskripsi ?: ""
        
        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            if (isAdded) parentFragmentManager.popBackStack()
        }

        if (type == "QUIZ") {
            ivIcon.setImageResource(R.drawable.ic_quiz)
            tvProgress.text = "Selesaikan kuis untuk klaim lencana"
            btnAksi.text = "Mulai Kuis Kilat"
            
            val ctx = context
            if (ctx != null && badgeId != null && BadgeHelper.punyaBadge(ctx, badgeId!!)) {
                btnAksi.text = "Lencana Sudah Didapat ✓"
                btnAksi.isEnabled = false
            } else {
                btnAksi.setOnClickListener {
                    if (!isAdded) return@setOnClickListener
                    quizId?.let { id ->
                        val fragment = QuizSoalFragment.newInstance(id)
                        fragment.arguments = (fragment.arguments ?: Bundle()).apply {
                            putString("badge_id", badgeId)
                            putString("challenge_id", bonusId)
                            putBoolean("IS_TANTANGAN_BONUS", true)
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack("BONUS_DETAIL")
                            .commit()
                    }
                }
            }
        } else {
            ivIcon.setImageResource(R.drawable.ic_tips)
            updateCommitmentUI(tvProgress, btnAksi)
        }
    }

    private fun updateCommitmentUI(tvProgress: TextView, btnAksi: Button) {
        val ctx = context ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        if (userId == "guest") return

        val pref = ctx.getSharedPreferences("BONUS_PROGRESS_$userId", Context.MODE_PRIVATE)
        
        val doneCount = pref.getInt("done_$bonusId", 0)
        val lastDate = pref.getString("last_date_$bonusId", "")
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        tvProgress.text = "Progres: $doneCount / $targetDays Hari"

        if (doneCount >= targetDays) {
            btnAksi.text = "Tantangan Selesai ✓"
            btnAksi.isEnabled = false
            // Otomatis sinkronkan lencana jika sudah selesai
            badgeId?.let { BadgeHelper.tambahBadge(ctx, it, false) }
        } else if (lastDate == today) {
            btnAksi.text = "Sudah Lapor Hari Ini"
            btnAksi.isEnabled = false
        } else {
            btnAksi.text = "Saya Sudah Melakukan Ini"
            btnAksi.isEnabled = true
            btnAksi.setOnClickListener {
                if (!isAdded) return@setOnClickListener
                val newCount = doneCount + 1
                pref.edit()
                    .putInt("done_$bonusId", newCount)
                    .putString("last_date_$bonusId", today)
                    .apply()
                
                if (newCount >= targetDays) {
                    // PANGGIL POPUP DI SINI
                    badgeId?.let { BadgeHelper.tambahBadge(ctx, it, true) }
                } else {
                    Toast.makeText(ctx, "Laporan diterima! Semangat!", Toast.LENGTH_SHORT).show()
                }
                updateCommitmentUI(tvProgress, btnAksi)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }
}
