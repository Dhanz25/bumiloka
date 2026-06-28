package com.faiz.bumiloka

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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
                    listSoal.add(Question(it.pertanyaan, opsi, correctIndex))
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
            progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Gagal memuat kuis", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadQuestion() {
        if (questions.isEmpty()) return
        
        val q = questions[currentQuestion]
        tvNumber.text = "Soal ${currentQuestion + 1}/${questions.size}"
        tvQuestion.text = q.question
        
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
        // Menghitung skor akhir dalam skala 0-100
        val finalScore = if (totalSoal > 0) (skor.toDouble() / (totalSoal * 10) * 100).toInt() else 0
        
        val bundle = Bundle().apply {
            putInt("BENAR", skor / 10)
            putInt("SALAH", totalSoal - (skor / 10))
            putInt("SKOR", finalScore)
            putString("KUIS_ID", kuisId)
            putInt("LEVEL", level)
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

    data class Question(val question: String, val options: List<String>, val correctAnswer: Int)
}
