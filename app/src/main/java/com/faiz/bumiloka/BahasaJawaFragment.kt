package com.faiz.bumiloka

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
    private lateinit var tvSoal: TextView
    private lateinit var rbA: RadioButton
    private lateinit var rbB: RadioButton
    private lateinit var rbC: RadioButton
    private lateinit var rbD: RadioButton
    private lateinit var radioGroupJawaban: RadioGroup
    private lateinit var rvMateriLingkungan: RecyclerView

    private val db = FirebaseFirestore.getInstance()
    private val hasilPerSoal = mutableListOf<Boolean>()
    private val listMateri = mutableListOf<MateriLingkunganJawa>()
    
    private var nomorSoal = 1
    private val totalSoal = 10
    private var jumlahBenar = 0
    private var jumlahSalah = 0
    private var jawabanBenar = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_bahasa_jawa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        MbahGuruFirestore.initialize(requireContext())
        
        tvJudulMateri = view.findViewById(R.id.tvJudulMateri)
        layoutQuiz = view.findViewById(R.id.layoutQuiz)
        layoutMateri = view.findViewById(R.id.layoutMateri)
        btnBackQuiz = view.findViewById(R.id.btnBackQuiz)
        btnBackMateri = view.findViewById(R.id.btnBackMateri)
        rvMateriLingkungan = view.findViewById(R.id.rvMateriLingkungan)
        rvMateriLingkungan.layoutManager = LinearLayoutManager(requireContext())
        tvSoal = view.findViewById(R.id.tvSoal)
        rbA = view.findViewById(R.id.rbA)
        rbB = view.findViewById(R.id.rbB)
        rbC = view.findViewById(R.id.rbC)
        rbD = view.findViewById(R.id.rbD)
        radioGroupJawaban = view.findViewById(R.id.radioGroupJawaban)
        btnSelanjutnya = view.findViewById(R.id.btnSelanjutnya)

        btnBackQuiz.setOnClickListener { activity?.onBackPressedDispatcher?.onBackPressed() }
        btnBackMateri.setOnClickListener { activity?.onBackPressedDispatcher?.onBackPressed() }

        btnSelanjutnya.setOnClickListener { handleNextQuestion() }

        cekStatusQuiz()
    }

    private fun loadSoal() {
        if (!isAdded) return
        db.collection("challenge_lingkungan")
            .document("soal$nomorSoal")
            .get()
            .addOnSuccessListener { document ->
                if (!isAdded || document == null) return@addOnSuccessListener
                
                tvSoal.text = document.getString("pertanyaan") ?: ""
                rbA.text = document.getString("opsiA") ?: ""
                rbB.text = document.getString("opsiB") ?: ""
                rbC.text = document.getString("opsiC") ?: ""
                rbD.text = document.getString("opsiD") ?: ""
                jawabanBenar = document.getString("jawaban")?.trim()?.uppercase() ?: ""

                radioGroupJawaban.clearCheck()
                btnSelanjutnya.text = if (nomorSoal == totalSoal) "Selesai" else "Selanjutnya"
            }
    }

    private fun handleNextQuestion() {
        val jawabanUser = when (radioGroupJawaban.checkedRadioButtonId) {
            R.id.rbA -> "A"
            R.id.rbB -> "B"
            R.id.rbC -> "C"
            R.id.rbD -> "D"
            else -> ""
        }

        if (jawabanUser.isEmpty()) {
            Toast.makeText(requireContext(), "Pilih jawaban terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (jawabanUser == jawabanBenar) jumlahBenar++ else jumlahSalah++

        if (nomorSoal < totalSoal) {
            nomorSoal++
            loadSoal()
        } else {
            tampilkanHasil()
        }
    }

    private fun tampilkanHasil() {
        if (!isAdded) return
        val dialogView = layoutInflater.inflate(R.layout.dialog_hasil_quiz, null)
        val skor = (jumlahBenar * 100) / totalSoal

        dialogView.findViewById<TextView>(R.id.tvBenar).text = "Benar : $jumlahBenar"
        dialogView.findViewById<TextView>(R.id.tvSalah).text = "Salah : $jumlahSalah"
        dialogView.findViewById<TextView>(R.id.tvSkor).text = "Skor : $skor/100"

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView).setCancelable(false).create()

        dialogView.findViewById<Button>(R.id.btnUlangi).setOnClickListener {
            nomorSoal = 1; jumlahBenar = 0; jumlahSalah = 0
            loadSoal()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnSelesai).setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseDatabase.getInstance().reference.child("users").child(uid).child("bahasa_jawa_selesai").setValue(true)
            }
            dialog.dismiss()
            layoutQuiz.visibility = View.GONE
            layoutMateri.visibility = View.VISIBLE
            loadMateriLingkungan()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun cekStatusQuiz() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference.child("users").child(uid).child("bahasa_jawa_selesai").get()
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
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

    private fun loadMateriLingkungan() {
        if (!isAdded) return
        val dbMbahGuru = MbahGuruFirestore.getFirestore()
        dbMbahGuru.collection("materi").document("materi_lingkungan").get()
            .addOnSuccessListener { doc ->
                if (isAdded) tvJudulMateri.text = doc.getString("judul") ?: ""
            }

        dbMbahGuru.collection("materi").document("materi_lingkungan").collection("items")
            .orderBy("urutan").get()
            .addOnSuccessListener { result ->
                if (!isAdded) return@addOnSuccessListener
                listMateri.clear()
                for (doc in result) {
                    doc.toObject(MateriLingkunganJawa::class.java).let { listMateri.add(it) }
                }
                rvMateriLingkungan.adapter = MateriLingkunganAdapter(listMateri)
            }
    }
}
