package com.faiz.bumiloka

import android.app.AlertDialog
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
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
    private var poinReward: Int = 20 // Default poin

    private lateinit var tvQuestion: TextView
    private lateinit var tvNumber: TextView
    private lateinit var ivQuestionImage: ImageView
    private lateinit var options: List<TextView>
    private lateinit var btnNext: Button
    private lateinit var progressBar: ProgressBar
    
    private var questions: List<Question> = listOf()

    companion object {
        fun newInstance(kuisId: String, level: Int = 1): QuizSoalFragment {
            return QuizSoalFragment().apply {
                arguments = Bundle().apply {
                    putString("KUIS_ID", kuisId)
                    putInt("LEVEL", level)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

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

        kuisId?.let { fetchKuisData(it) } ?: run {
            if (isAdded) {
                Toast.makeText(requireContext(), "ID Kuis tidak ditemukan", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
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

    private fun fetchKuisData(id: String) {
        if (!isAdded) return
        progressBar.visibility = View.VISIBLE
        // Ambil seluruh data kuis untuk mendapatkan poinReward
        val db = FirebaseDatabase.getInstance().getReference("kuis").child(id)
        
        db.get().addOnSuccessListener { snapshot ->
            if (!isAdded) return@addOnSuccessListener
            progressBar.visibility = View.GONE
            
            // Ambil poinReward dari Firebase (Kuis metadata)
            poinReward = snapshot.child("poinReward").getValue(Int::class.java) ?: 20
            
            val soalSnapshot = snapshot.child("soal")
            val listSoal = mutableListOf<Question>()
            
            soalSnapshot.children.forEach { child ->
                try {
                    val map = child.value as? Map<*, *> ?: return@forEach
                    val pert = map["pertanyaan"]?.toString() ?: ""
                    val oA = map["opsiA"]?.toString() ?: ""
                    val oB = map["opsiB"]?.toString() ?: ""
                    val oC = map["opsiC"]?.toString() ?: ""
                    val oD = map["opsiD"]?.toString() ?: ""
                    val jaw = map["jawabanBenar"]?.toString() ?: "A"
                    val img = map["imageUrl"]?.toString() ?: ""

                    val opsiOriginal = listOf(oA, oB, oC, oD)
                    val correctText = when (jaw.uppercase()) {
                        "A" -> oA
                        "B" -> oB
                        "C" -> oC
                        "D" -> oD
                        else -> oA
                    }
                    
                    val opsiShuffled = opsiOriginal.shuffled()
                    val correctIndex = opsiShuffled.indexOf(correctText)
                    
                    listSoal.add(Question(pert, opsiShuffled, correctIndex, img))
                } catch (e: Exception) { Log.e("QuizSoal", "Error parsing soal: ${e.message}") }
            }

            if (listSoal.isNotEmpty()) {
                questions = listSoal.shuffled()
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
        if (!isAdded || questions.isEmpty()) return
        
        val q = questions[currentQuestion]
        tvNumber.text = "Soal ${currentQuestion + 1}/${questions.size}"
        tvQuestion.text = q.question
        
        if (!q.imageUrl.isNullOrEmpty()) {
            ivQuestionImage.visibility = View.VISIBLE
            if (q.imageUrl.length > 100 || q.imageUrl.startsWith("http")) {
                val source = if (q.imageUrl.length > 100 && !q.imageUrl.startsWith("http")) 
                    Base64.decode(q.imageUrl, Base64.DEFAULT) else q.imageUrl
                Glide.with(this).load(source).placeholder(R.drawable.img_lingkungan).into(ivQuestionImage)
            } else {
                val resId = resources.getIdentifier(q.imageUrl, "drawable", activity?.packageName ?: "")
                ivQuestionImage.setImageResource(if (resId != 0) resId else R.drawable.img_lingkungan)
            }
        } else { ivQuestionImage.visibility = View.GONE }

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
        if (!isAdded) return
        for (option in options) option.setBackgroundResource(R.drawable.bg_option)
        selectedAnswer = index
        sudahPilih = true
        options[index].setBackgroundResource(android.R.color.holo_green_light)
        btnNext.isEnabled = true
    }

    private fun pindahKeHasil(level: Int) {
        if (!isAdded) return
        val totalSoal = questions.size
        val finalScore = if (totalSoal > 0) (skor.toDouble() / (totalSoal * 10) * 100).toInt() else 0
        
        val bundle = Bundle().apply {
            putInt("BENAR", skor / 10)
            putInt("SALAH", totalSoal - (skor / 10))
            putInt("SKOR", finalScore)
            putString("KUIS_ID", kuisId)
            putInt("LEVEL", level)
            putInt("POIN_REWARD", poinReward) // Kirim poinReward ke fragment hasil
            
            arguments?.let { args ->
                putString("challenge_id", args.getString("challenge_id"))
                putString("badge_id", args.getString("badge_id"))
                putString("quiz_id", args.getString("quiz_id"))
                putString("materi_id", args.getString("materi_id"))
                putBoolean("IS_TANTANGAN_BONUS", args.getBoolean("IS_TANTANGAN_BONUS", false))
                putBoolean("DARI_MISI", args.getBoolean("DARI_MISI", false))
            }
        }

        val fragment = QuizMenang1Fragment()
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showExitDialog() {
        if (!isAdded) return
        val dialogView = layoutInflater.inflate(R.layout.popup_konfirmasikeluar, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(false).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogView.findViewById<Button>(R.id.btnBatal).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnKeluar).setOnClickListener {
            dialog.dismiss()
            if (isAdded) parentFragmentManager.popBackStack()
        }
        dialog.show()
    }

    data class Question(val question: String, val options: List<String>, val correctAnswer: Int, val imageUrl: String? = null)
}
