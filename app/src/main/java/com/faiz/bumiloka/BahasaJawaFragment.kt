package com.faiz.bumiloka

import android.media.Image
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.adapters.MateriLingkunganAdapter
import com.faiz.bumiloka.model.MateriLingkunganJawa
import com.faiz.bumiloka.network.MbahGuruFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class BahasaJawaFragment : Fragment() {

    private lateinit var tvJudulMateri: TextView
    private lateinit var btnBackQuiz: ImageView
    private lateinit var btnBackMateri: ImageView
    private lateinit var layoutQuiz: LinearLayout
    private lateinit var layoutMateri: LinearLayout

    private lateinit var btnSelanjutnya: Button
    private val db = FirebaseFirestore.getInstance()
    private lateinit var tvSoal: TextView
    private val hasilPerSoal = mutableListOf<Boolean>()
    private var nomorSoal = 1
    private val totalSoal = 10
    private var jumlahBenar = 0
    private var jumlahSalah = 0

    private lateinit var rbA: RadioButton
    private lateinit var rbB: RadioButton
    private lateinit var rbC: RadioButton
    private lateinit var rbD: RadioButton

    private lateinit var radioGroupJawaban: RadioGroup

    private var jawabanBenar = ""
    private fun loadSoal() {

        db.collection("challenge_lingkungan")
            .document("soal$nomorSoal")
            .get()
            .addOnSuccessListener { document ->

                val pertanyaan =
                    document.getString("pertanyaan")

                val opsiA =
                    document.getString("opsiA")

                val opsiB =
                    document.getString("opsiB")

                val opsiC =
                    document.getString("opsiC")

                val opsiD =
                    document.getString("opsiD")

                jawabanBenar =
                    document.getString("jawaban")
                        ?.trim()
                        ?.uppercase()
                        ?: ""

                tvSoal.text = pertanyaan

                rbA.text = opsiA
                rbB.text = opsiB
                rbC.text = opsiC
                rbD.text = opsiD

                radioGroupJawaban.clearCheck()

                if (nomorSoal == totalSoal) {
                    btnSelanjutnya.text = "Selesai"
                } else {
                    btnSelanjutnya.text = "Selanjutnya"
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_bahasa_jawa,
            container,
            false
        )
        MbahGuruFirestore.initialize(requireContext())
        val dbMbahGuru = MbahGuruFirestore.getFirestore()

//        dbMbahGuru
//            .collection("materi")
//            .document("materi_lingkungan")
//            .collection("items")
//            .get()
//            .addOnSuccessListener { result ->
//
//                for (doc in result) {
//
//                    Toast.makeText(
//                        requireContext(),
//                        doc.getString("nama"),
//                        Toast.LENGTH_SHORT
//                    ).show()
//
//                }
//            }

        tvJudulMateri =
            view.findViewById(R.id.tvJudulMateri)
        layoutQuiz = view.findViewById(R.id.layoutQuiz)
        layoutMateri = view.findViewById(R.id.layoutMateri)
        btnBackQuiz = view.findViewById(R.id.btnBackQuiz)
        btnBackMateri =
            view.findViewById(R.id.btnBackMateri)
        rvMateriLingkungan =
            view.findViewById(R.id.rvMateriLingkungan)
        rvMateriLingkungan.layoutManager =
            LinearLayoutManager(requireContext())

        tvSoal = view.findViewById(R.id.tvSoal)

        rbA = view.findViewById(R.id.rbA)
        rbB = view.findViewById(R.id.rbB)
        rbC = view.findViewById(R.id.rbC)
        rbD = view.findViewById(R.id.rbD)

        radioGroupJawaban =
            view.findViewById(R.id.radioGroupJawaban)
        cekStatusQuiz()

        btnBackQuiz.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnBackMateri.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnSelanjutnya = view.findViewById(R.id.btnSelanjutnya)
        btnSelanjutnya.setOnClickListener {
            val jawabanUser = when {
                rbA.isChecked -> "A"
                rbB.isChecked -> "B"
                rbC.isChecked -> "C"
                rbD.isChecked -> "D"
                else -> ""
            }.trim().uppercase()

            if (jawabanUser.isEmpty()) {

                Toast.makeText(
                    requireContext(),
                    "Pilih jawaban terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Hitung benar/salah
            if (jawabanUser == jawabanBenar) {

                jumlahBenar++
                hasilPerSoal.add(true)

            } else {

                jumlahSalah++
                hasilPerSoal.add(false)
            }

            // Soal berikutnya
            if (nomorSoal < totalSoal) {

                nomorSoal++
                loadSoal()

            } else {

                tampilkanHasil()
            }
        }

        return view
    }
    private fun tampilkanHasil() {

        val dialogView = layoutInflater.inflate(
            R.layout.dialog_hasil_quiz,
            null
        )

        val skor = (jumlahBenar * 100) / totalSoal

        val tvBenar =
            dialogView.findViewById<TextView>(R.id.tvBenar)

        val tvSalah =
            dialogView.findViewById<TextView>(R.id.tvSalah)

        val tvSkor =
            dialogView.findViewById<TextView>(R.id.tvSkor)

        val btnUlangi =
            dialogView.findViewById<Button>(R.id.btnUlangi)

        val btnSelesai =
            dialogView.findViewById<Button>(R.id.btnSelesai)

        tvBenar.text = "Benar : $jumlahBenar"
        tvSalah.text = "Salah : $jumlahSalah"
        tvSkor.text = "Skor : $skor/100"

        val dialog = androidx.appcompat.app.AlertDialog.Builder(
            requireContext()
        )
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        btnUlangi.setOnClickListener {

            nomorSoal = 1
            jumlahBenar = 0
            jumlahSalah = 0
            hasilPerSoal.clear()

            loadSoal()

            dialog.dismiss()
        }

        btnSelesai.setOnClickListener {

            val uid = FirebaseAuth.getInstance().currentUser?.uid

            if (uid != null) {
                FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("bahasa_jawa_selesai")
                    .setValue(true)
            }

            dialog.dismiss()

            layoutQuiz.visibility = View.GONE
            layoutMateri.visibility = View.VISIBLE
            loadMateriLingkungan()
        }
    }
    private fun cekStatusQuiz() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .child("bahasa_jawa_selesai")
            .get()
            .addOnSuccessListener {

                val selesai = it.getValue(Boolean::class.java) ?: false

                if (selesai) {

                    layoutQuiz.visibility = View.GONE
                    layoutMateri.visibility = View.VISIBLE

                    loadMateriLingkungan()

                } else {

                    loadSoal()
                }
            }
    }
    private lateinit var rvMateriLingkungan: RecyclerView

    private val listMateri =
        mutableListOf<MateriLingkunganJawa>()
    private fun loadMateriLingkungan() {

        val dbMbahGuru =
            MbahGuruFirestore.getFirestore()
        dbMbahGuru
            .collection("materi")
            .document("materi_lingkungan")
            .get()
            .addOnSuccessListener { document ->

                tvJudulMateri.text =
                    document.getString("judul") ?: ""

            }
        dbMbahGuru
            .collection("materi")
            .document("materi_lingkungan")
            .collection("items")
            .orderBy("urutan")
            .get()
            .addOnSuccessListener { result ->

                listMateri.clear()

                for (doc in result) {

                    val materi =
                        doc.toObject(
                            MateriLingkunganJawa::class.java
                        )

                    listMateri.add(materi)
                }

                rvMateriLingkungan.adapter =
                    MateriLingkunganAdapter(listMateri)
            }
    }
}
