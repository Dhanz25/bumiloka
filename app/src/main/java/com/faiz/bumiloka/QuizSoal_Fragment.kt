package com.faiz.bumiloka

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar

class QuizSoalFragment : Fragment(R.layout.fragment_quiz_soal_) {

    private var currentQuestion = 0
    private var skor = 0
    private var sudahPilih = false

    private var selectedAnswer = -1

    private lateinit var tvQuestion: TextView
    private lateinit var tvNumber: TextView
    private lateinit var options: List<TextView>
    private lateinit var btnNext: Button



    // ✅ 10 SOAL
    private val questions = listOf(

        Question(
            "Apa tindakan paling tepat untuk menjaga kebersihan lingkungan?",
            listOf(
                "Membuang sampah pada tempatnya",
                "Membuang sampah ke sungai",
                "Membakar semua sampah",
                "Membiarkan sampah menumpuk"
            ),
            0
        ),

        Question(
            "Mengapa kita perlu menanam pohon?",
            listOf(
                "Untuk menambah polusi",
                "Untuk menghasilkan oksigen",
                "Untuk mengurangi udara segar",
                "Untuk mempercepat pemanasan global"
            ),
            1
        ),

        Question(
            "Apa yang sebaiknya dilakukan dengan sampah plastik?",
            listOf(
                "Dibuang ke laut",
                "Didaur ulang",
                "Dibakar sembarangan",
                "Dibiarkan menumpuk"
            ),
            1
        ),

        Question(
            "Cara sederhana menghemat listrik di rumah adalah?",
            listOf(
                "Menyalakan semua lampu",
                "Mematikan alat listrik saat tidak digunakan",
                "Membiarkan TV menyala terus",
                "Menggunakan listrik tanpa batas"
            ),
            1
        ),

        Question(
            "Apa dampak membuang sampah sembarangan?",
            listOf(
                "Lingkungan menjadi bersih",
                "Terjadi banjir dan pencemaran",
                "Udara menjadi segar",
                "Tidak ada dampak"
            ),
            1
        ),

        Question(
            "Mengapa kita harus menghemat air?",
            listOf(
                "Agar air cepat habis",
                "Agar tersedia untuk masa depan",
                "Supaya bisa boros",
                "Agar tidak digunakan orang lain"
            ),
            1
        ),

        Question(
            "Apa yang dimaksud dengan daur ulang?",
            listOf(
                "Membuang sampah",
                "Mengolah kembali sampah menjadi barang baru",
                "Membakar sampah",
                "Menimbun sampah"
            ),
            1
        ),

        Question(
            "Contoh energi terbarukan adalah?",
            listOf(
                "Batu bara",
                "Minyak bumi",
                "Energi matahari",
                "Gas alam"
            ),
            2
        ),

        Question(
            "Bagaimana cara menjaga kebersihan sekolah?",
            listOf(
                "Membuang sampah sembarangan",
                "Membersihkan kelas secara rutin",
                "Merusak fasilitas sekolah",
                "Mencoret-coret dinding"
            ),
            1
        ),

        Question(
            "Cara mengurangi penggunaan plastik adalah?",
            listOf(
                "Menggunakan plastik sekali pakai",
                "Membawa tas belanja sendiri",
                "Membuang plastik sembarangan",
                "Membakar plastik"
            ),
            1
        )
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        btnNext = view.findViewById(R.id.btnNext)
        tvQuestion = view.findViewById(R.id.tvQuestion)
        tvNumber = view.findViewById(R.id.tvNumber)

        options = listOf(
            view.findViewById(R.id.option1),
            view.findViewById(R.id.option2),
            view.findViewById(R.id.option3),
            view.findViewById(R.id.option4)
        )

        toolbar.setNavigationOnClickListener {
            showExitDialog()
        }

        btnNext.isEnabled = false

        loadQuestion()

        btnNext.setOnClickListener {
            if (sudahPilih) {

                val q = questions[currentQuestion]

                // cek jawaban benar saat NEXT ditekan
                if (selectedAnswer == q.correctAnswer) {
                    skor += 10
                }

                currentQuestion++

                if (currentQuestion < questions.size) {
                    loadQuestion()
                } else {
                    pindahKeHasil()
                }
            }
        }
    }

    private fun showExitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.popup_konfirmasikeluar, null)

        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val btnKeluar = dialogView.findViewById<Button>(R.id.btnKeluar)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnKeluar.setOnClickListener {
            dialog.dismiss()
            parentFragmentManager.popBackStack()
        }

        dialog.show()
    }

    private fun loadQuestion() {
        val q = questions[currentQuestion]

        tvNumber.text = "Soal ${currentQuestion + 1}/10"
        tvQuestion.text = q.question

        for (i in options.indices) {
            options[i].text = "${('A' + i)}. ${q.options[i]}"
            options[i].setBackgroundResource(R.drawable.bg_option)

            options[i].setOnClickListener {
                pilihJawaban(i, q.correctAnswer)
            }
        }

        sudahPilih = false
        selectedAnswer = -1
        btnNext.isEnabled = false
    }

    private fun pilihJawaban(index: Int, correct: Int) {

        // reset tampilan semua opsi
        for (option in options) {
            option.setBackgroundResource(R.drawable.bg_option)
        }

        // simpan pilihan terbaru
        selectedAnswer = index
        sudahPilih = true

        // highlight pilihan
        options[index].setBackgroundResource(android.R.color.holo_green_light)

        // aktifkan tombol next
        btnNext.isEnabled = true
    }

    private fun pindahKeHasil() {

        val totalSoal = 10
        val benar = skor / 10
        val salah = totalSoal - benar

        val prefs = requireActivity().getSharedPreferences("KUIS", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("materi1_selesai", true)
            .putInt("nilai_materi1", skor)
            .apply()

        val bundle = Bundle()
        bundle.putInt("BENAR", benar)
        bundle.putInt("SALAH", salah)
        bundle.putInt("SKOR", skor)

        val fragment = QuizMenang1Fragment()
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    data class Question(
        val question: String,
        val options: List<String>,
        val correctAnswer: Int
    )
}