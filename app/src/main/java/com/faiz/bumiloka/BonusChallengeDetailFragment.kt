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
        fun newInstance(challengeId: String): BonusChallengeDetailFragment {
            val fragment = BonusChallengeDetailFragment()
            val args = Bundle()
            args.putString("challenge_id", challengeId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        challengeId = arguments?.getString("challenge_id") ?: ""

        tvTitle = view.findViewById(R.id.tvTitle)
        tvDescription = view.findViewById(R.id.tvDescription)
        tvMateriName = view.findViewById(R.id.tvMateriName)
        tvQuizName = view.findViewById(R.id.tvQuizName)
        tvBadgeName = view.findViewById(R.id.tvBadgeName)
        ivMateriStatus = view.findViewById(R.id.ivMateriStatus)
        ivQuizStatus = view.findViewById(R.id.ivQuizStatus)
        btnAction = view.findViewById(R.id.btnAction)
        btnBack = view.findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        loadChallengeData()
        observeProgress()
    }

    private fun loadChallengeData() {
        BonusChallengeRepository.getActiveChallenges { challenges ->
            challenge = challenges.find { it.id == challengeId }
            challenge?.let {
                tvTitle.text = it.judul
                tvDescription.text = it.deskripsi
                tvMateriName.text = "Materi ${it.materiId}"
                tvQuizName.text = "Quiz ${it.quizId}"
                tvBadgeName.text = it.badgeId
                updateUI()
            }
        }
    }

    private fun observeProgress() {
        BonusChallengeRepository.getChallengeProgress(challengeId) {
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
            btnAction.text = "SELESAI"
            btnAction.isEnabled = false
            btnAction.setBackgroundColor(colorNotDone)
        } else if (progress.materiDone && progress.quizDone) {
            btnAction.text = "TANDAI SELESAI"
            btnAction.isEnabled = true
            btnAction.setOnClickListener {
                showSuccessDialog()
            }
        } else {
            btnAction.text = "LANJUT"
            btnAction.isEnabled = true
            btnAction.setOnClickListener {
                startChallengeFlow()
            }
        }
    }

    private fun startChallengeFlow() {
        val currentChallenge = challenge ?: return
        if (!progress.materiDone) {
            val fragment = MateriFragment.newInstanceLegacy(currentChallenge.materiId)
            val args = fragment.arguments ?: Bundle()
            args.putBoolean("DARI_BONUS_CHALLENGE", true)
            args.putString("BONUS_CHALLENGE_ID", challengeId)
            args.putInt("BONUS_QUIZ_ID", currentChallenge.quizId)
            fragment.arguments = args
            
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        } else if (!progress.quizDone) {
            openQuiz(currentChallenge.quizId)
        }
    }

    private fun openQuiz(quizId: Int) {
        LevelHelper.getCurrentLevel(requireContext()) { level ->
            val fragment = when (quizId) {
                1 -> QuizSoalFragment.newInstance(quizId)
                2 -> QuizSoal2Fragment.newInstance(quizId)
                3 -> QuizSoal3Fragment.newInstance(quizId)
                else -> QuizSoalFragment.newInstance(quizId)
            }
            val args = fragment.arguments ?: Bundle()
            args.putBoolean("DARI_BONUS_CHALLENGE", true)
            args.putString("BONUS_CHALLENGE_ID", challengeId)
            args.putInt("LEVEL", level)
            fragment.arguments = args

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Selamat!")
            .setMessage("Anda berhasil menyelesaikan tantangan.\n\nBadge yang diperoleh:\n${challenge?.badgeId}")
            .setPositiveButton("OK") { _, _ ->
                challenge?.let {
                    BadgeHelper.tambahBadge(requireContext(), it.badgeId)
                    BonusChallengeRepository.markAsCompleted(challengeId) {
                        // Progress updated by listener
                    }
                }
            }
            .show()
    }
}
