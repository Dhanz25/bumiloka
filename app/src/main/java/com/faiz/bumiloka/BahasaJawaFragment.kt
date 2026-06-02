package com.faiz.bumiloka

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast

class BahasaJawaFragment : Fragment() {

    private lateinit var layoutQuiz: LinearLayout
    private lateinit var layoutMateri: LinearLayout
    private lateinit var rbBenar: RadioButton
    private lateinit var btnSelanjutnya: Button

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

        layoutQuiz = view.findViewById(R.id.layoutQuiz)
        layoutMateri = view.findViewById(R.id.layoutMateri)
        rbBenar = view.findViewById(R.id.rbBenar)
        btnSelanjutnya = view.findViewById(R.id.btnSelanjutnya)

        btnSelanjutnya.setOnClickListener {
            if (rbBenar.isChecked) {
                // Berikan reward poin (XP)
                AktivitasHelper.tambahPoint(20)
                AktivitasHelper.tambahMisiSelesai()

                Toast.makeText(requireContext(), "Jawaban Benar! +20 Poin", Toast.LENGTH_SHORT).show()

                // Ganti tampilan ke Materi
                layoutQuiz.visibility = View.GONE
                layoutMateri.visibility = View.VISIBLE
            } else {
                Toast.makeText(
                    requireContext(),
                    "Jawaban salah. Ayo coba lagi! 🌱",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return view
    }
}
