package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.data.BonusChallengeRepository
import com.faiz.bumiloka.model.BonusChallengeModel
import com.faiz.bumiloka.model.ChallengeProgress

class BonusChallengeDetailFragment : Fragment(R.layout.fragment_bonus_challenge_detail) {

    private var challengeId: String = ""
    private var challenge: BonusChallengeModel? = null
    private var progress: ChallengeProgress = ChallengeProgress()

    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvMateriName: TextView
    private lateinit var tvQuizName: TextView
    private lateinit var tvBadgeName: TextView
    private lateinit var ivMateriStatus: ImageView
    private lateinit var ivQuizStatus: ImageView
    private lateinit var btnAction: Button
    private lateinit var btnBack: ImageButton

    companion object {
        @JvmStatic
        fun newInstance(challengeId: String): BonusChallengeDetailFragment {
            return BonusChallengeDetailFragment().apply {
                arguments = Bundle().apply { putString("challenge_id", challengeId) }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        challengeId = arguments?.getString("challenge_id") ?: ""

        initViews(view)
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        loadChallengeData()
    }

    private fun initViews(view: View) {
        tvTitle = view.findViewById(R.id.tvTitle)
        tvDescription = view.findViewById(R.id.tvDescription)
        tvMateriName = view.findViewById(R.id.tvMateriName)
        tvQuizName = view.findViewById(R.id.tvQuizName)
        tvBadgeName = view.findViewById(R.id.tvBadgeName)
        ivMateriStatus = view.findViewById(R.id.ivMateriStatus)
        ivQuizStatus = view.findViewById(R.id.ivQuizStatus)
        btnAction = view.findViewById(R.id.btnAction)
        btnBack = view.findViewById(R.id.btnBack)
    }

    private fun loadChallengeData() {
        BonusChallengeRepository.getActiveChallenges { challenges ->
            if (!isAdded) return@getActiveChallenges
            challenge = challenges.find { it.id == challengeId }
            challenge?.let {
                tvTitle.text = it.judul
                tvDescription.text = it.deskripsi
                tvMateriName.text = "Materi: ${it.materiId}"
                tvQuizName.text = "Quiz: ${it.quizId}"
                tvBadgeName.text = "Hadiah: ${it.badgeId}"
                observeProgress()
            }
        }
    }

    private fun observeProgress() {
        BonusChallengeRepository.getChallengeProgress(challengeId) {
            if (!isAdded) return@getChallengeProgress
            progress = it
            updateUI()
        }
    }

    private fun updateUI() {
        if (!isAdded) return
        val context = requireContext()
        val colorDone = ContextCompat.getColor(context, R.color.nav_active)
        val colorNotDone = ContextCompat.getColor(context, R.color.grey_button)

        ivMateriStatus.setColorFilter(if (progress.materiDone) colorDone else colorNotDone)
        ivQuizStatus.setColorFilter(if (progress.quizDone) colorDone else colorNotDone)

        if (progress.completed) {
            btnAction.text = "SUDAH SELESAI ✓"
            btnAction.isEnabled = false
            btnAction.setBackgroundColor(colorNotDone)
        } else if (progress.materiDone && progress.quizDone) {
            btnAction.text = "KLAIM HADIAH"
            btnAction.isEnabled = true
            btnAction.setOnClickListener { 
                // Gunakan BadgeHelper dengan popup
                challenge?.let { c ->
                    BadgeHelper.tambahBadge(requireContext(), c.badgeId, true)
                    AktivitasHelper.tambahPoint(requireContext(), 100, "Tantangan Bonus")
                    BonusChallengeRepository.markAsCompleted(challengeId) {
                        if (isAdded) updateUI() 
                    }
                }
            }
        } else {
            btnAction.text = if (!progress.materiDone) "PELAJARI MATERI" else "KERJAKAN KUIS"
            btnAction.isEnabled = true
            btnAction.setOnClickListener { startChallengeFlow() }
        }
    }

    private fun startChallengeFlow() {
        val currentChallenge = challenge ?: return
        if (!progress.materiDone) {
            val fragment = MateriFragment.newInstanceForChallenge(currentChallenge.materiId)
            val bundle = fragment.arguments ?: Bundle()
            bundle.putBoolean("IS_TANTANGAN_BONUS", true)
            bundle.putString("challenge_id", challengeId)
            bundle.putString("quiz_id", currentChallenge.quizId)
            bundle.putString("badge_id", currentChallenge.badgeId)
            bundle.putString("materi_id", currentChallenge.materiId)
            fragment.arguments = bundle
            
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("BONUS_DETAIL")
                .commit()
        } else if (!progress.quizDone) {
            openQuiz(currentChallenge.quizId)
        }
    }

    private fun openQuiz(quizId: String) {
        LevelHelper.getCurrentLevel(requireContext()) { level ->
            if (!isAdded) return@getCurrentLevel
            val fragment = when (quizId) {
                "2" -> QuizSoal2Fragment()
                "3" -> QuizSoal3Fragment()
                else -> QuizSoalFragment.newInstance(quizId, level)
            }
            
            val bundle = fragment.arguments ?: Bundle()
            bundle.putBoolean("IS_TANTANGAN_BONUS", true)
            bundle.putString("challenge_id", challengeId)
            bundle.putString("quiz_id", quizId)
            bundle.putString("badge_id", challenge?.badgeId ?: "")
            bundle.putString("materi_id", challenge?.materiId ?: "")
            bundle.putInt("LEVEL", level)
            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("BONUS_DETAIL")
                .commit()
        }
    }
}
