package com.faiz.bumiloka

import android.app.AlertDialog
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.FirebaseDatabase
import com.faiz.bumiloka.model.SoalKuis

class QuizSoalFragment : Fragment(R.layout.fragment_quiz_soal_) {

    private var currentQuestion = 0
    private var skor = 0
    private var sudahPilih = false
    private var selectedAnswer = -1
    private var kuisId: String? = null

    private lateinit var tvQuestion: TextView
    private lateinit var tvNumber: TextView
    private lateinit var ivQuestionImage: ImageView
    private lateinit var options: List<TextView>
    private lateinit var btnNext: Button
    private lateinit var progressBar: ProgressBar
    
    private var questions: List<Question> = listOf()

    companion object {
        fun newInstance(kuisId: String, level: Int = 1): QuizSoalFragment {
            val fragment = QuizSoalFragment()
            val args = Bundle()
            args.putString("KUIS_ID", kuisId)
            args.putInt("LEVEL", level)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

        kuisId = arguments?.getString("KUIS_ID")
        val level = arguments?.getInt("LEVEL") ?: 1

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        btnNext = view.findViewById(R.id.btnNext)
        tvQuestion = view.findViewById(R.id.tvQuestion)
        tvNumber = view.findViewById(R.id.tvNumber)
        ivQuestionImage = view.findViewById(R.id.ivQuestionImage)
        progressBar = view.findViewById(R.id.progress_bar)

        options = listOf(
            view.findViewById(R.id.option1),
            view.findViewById(R.id.option2),
            view.findViewById(R.id.option3),
            view.findViewById(R.id.option4)
        )

        toolbar.setNavigationOnClickListener { showExitDialog() }
        btnNext.isEnabled = false

        if (kuisId != null) {
            fetchSoalFromFirebase(kuisId!!)
        } else {
            Toast.makeText(requireContext(), "ID Kuis tidak ditemukan", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        btnNext.setOnClickListener {
            if (sudahPilih) {
                if (selectedAnswer == questions[currentQuestion].correctAnswer) {
                    skor += 10
                }
                currentQuestion++
                if (currentQuestion < questions.size) {
                    loadQuestion()
                } else {
                    pindahKeHasil(level)
                }
            }
        }
    }

    private fun fetchSoalFromFirebase(id: String) {
        progressBar.visibility = View.VISIBLE
        val db = FirebaseDatabase.getInstance().getReference("kuis").child(id).child("soal")
        
        db.get().addOnSuccessListener { snapshot ->
            if (!isAdded) return@addOnSuccessListener
            progressBar.visibility = View.GONE
            val listSoal = mutableListOf<Question>()
            
            snapshot.children.forEach { child ->
                val s = child.getValue(SoalKuis::class.java)
                s?.let {
                    val opsi = listOf(it.opsiA, it.opsiB, it.opsiC, it.opsiD)
                    val correctIndex = when (it.jawabanBenar.uppercase()) {
                        "A" -> 0
                        "B" -> 1
                        "C" -> 2
                        "D" -> 3
                        else -> 0
                    }
                    listSoal.add(Question(it.pertanyaan, opsi, correctIndex, it.imageUrl))
                }
            }

            if (listSoal.isNotEmpty()) {
                questions = listSoal
                loadQuestion()
            } else {
                Toast.makeText(requireContext(), "Kuis ini belum memiliki soal", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }.addOnFailureListener {
            if (isAdded) {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Gagal memuat kuis", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadQuestion() {
        if (questions.isEmpty()) return
        
        val q = questions[currentQuestion]
        tvNumber.text = "Soal ${currentQuestion + 1}/${questions.size}"
        tvQuestion.text = q.question
        
        // Handle Image
        if (!q.imageUrl.isNullOrEmpty()) {
            ivQuestionImage.visibility = View.VISIBLE
            if (q.imageUrl.length > 100) {
                try {
                    val imageBytes = Base64.decode(q.imageUrl, Base64.DEFAULT)
                    Glide.with(this)
                        .asBitmap()
                        .load(imageBytes)
                        .placeholder(R.drawable.img_lingkungan)
                        .into(ivQuestionImage)
                } catch (e: Exception) {
                    ivQuestionImage.setImageResource(R.drawable.img_lingkungan)
                }
            } else {
                val resId = resources.getIdentifier(q.imageUrl, "drawable", requireContext().packageName)
                ivQuestionImage.setImageResource(if (resId != 0) resId else R.drawable.img_lingkungan)
            }
        } else {
            ivQuestionImage.visibility = View.GONE
        }

        for (i in options.indices) {
            options[i].text = "${('A' + i)}. ${q.options[i]}"
            options[i].setBackgroundResource(R.drawable.bg_option)
            options[i].setOnClickListener { pilihJawaban(i) }
        }
        sudahPilih = false
        selectedAnswer = -1
        btnNext.isEnabled = false
    }

    private fun pilihJawaban(index: Int) {
        for (option in options) option.setBackgroundResource(R.drawable.bg_option)
        selectedAnswer = index
        sudahPilih = true
        options[index].setBackgroundResource(android.R.color.holo_green_light)
        btnNext.isEnabled = true
    }

    private fun pindahKeHasil(level: Int) {
        val totalSoal = questions.size
        val finalScore = if (totalSoal > 0) (skor.toDouble() / (totalSoal * 10) * 100).toInt() else 0
        
        val bundle = Bundle().apply {
            putInt("BENAR", skor / 10)
            putInt("SALAH", totalSoal - (skor / 10))
            putInt("SKOR", finalScore)
            putString("KUIS_ID", kuisId)
            putInt("LEVEL", level)
            
            // Pass metadata tantangan jika ada
            arguments?.getString("challenge_id")?.let { putString("challenge_id", it) }
            arguments?.getString("badge_id")?.let { putString("badge_id", it) }
            arguments?.getString("quiz_id")?.let { putString("quiz_id", it) }
            arguments?.getString("materi_id")?.let { putString("materi_id", it) }
            arguments?.getBoolean("IS_TANTANGAN_BONUS")?.let { putBoolean("IS_TANTANGAN_BONUS", it) }
        }

        val fragment = QuizMenang1Fragment()
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showExitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.popup_konfirmasikeluar, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(false).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogView.findViewById<Button>(R.id.btnBatal).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnKeluar).setOnClickListener {
            dialog.dismiss()
            parentFragmentManager.popBackStack()
        }
        dialog.show()
    }

    data class Question(
        val question: String,
        val options: List<String>,
        val correctAnswer: Int,
        val imageUrl: String? = null
    )
}