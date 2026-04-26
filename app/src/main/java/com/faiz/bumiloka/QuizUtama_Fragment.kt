package com.faiz.bumiloka

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class QuizUtamaFragment : Fragment(R.layout.fragment_quiz_utama_) {

    private lateinit var btnMateri1: Button
    private lateinit var tvStatus1: TextView
    private lateinit var btnMateri2: Button
    private lateinit var tvStatus2: TextView
    private lateinit var btnMateri3: Button
    private lateinit var tvStatus3: TextView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivBack = view.findViewById<ImageView>(R.id.ivBack)
        btnMateri1 = view.findViewById(R.id.btn_detail_materi1)
        tvStatus1 = view.findViewById(R.id.tvStatus1)
        btnMateri2 = view.findViewById(R.id.btn_kerjakan_materi2)
        tvStatus2 = view.findViewById(R.id.tvStatus2)
        btnMateri3 = view.findViewById(R.id.btn_kerjakan_materi3)
        tvStatus3 = view.findViewById(R.id.tvStatus3)

        val tabAll = view.findViewById<TextView>(R.id.tab_all)
        val tabSelesai = view.findViewById<TextView>(R.id.tab_selesai)
        val tabBelum = view.findViewById<TextView>(R.id.tab_belum)

        val card1 = view.findViewById<MaterialCardView>(R.id.card1)
        val card2 = view.findViewById<MaterialCardView>(R.id.card2)
        val card3 = view.findViewById<MaterialCardView>(R.id.card3)

        loadUI()

        ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        fun resetTab() {
            tabAll.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            tabSelesai.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            tabBelum.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        }

        fun filterCards(type: String) {
            val pref = requireActivity().getSharedPreferences("KUIS", Context.MODE_PRIVATE)
            val s1 = pref.getBoolean("materi1_selesai", false)
            val s2 = pref.getBoolean("quiz2_selesai", false)
            resetTab()
            when (type) {
                "ALL" -> {
                    tabAll.setBackgroundResource(R.drawable.bg_tab_active_new)
                    card1.visibility = View.VISIBLE
                    card2.visibility = View.VISIBLE
                    card3.visibility = View.VISIBLE
                }
                "SELESAI" -> {
                    tabSelesai.setBackgroundResource(R.drawable.bg_tab_active_new)
                    card1.visibility = if (s1) View.VISIBLE else View.GONE
                    card2.visibility = if (s2) View.VISIBLE else View.GONE
                    card3.visibility = View.GONE
                }
                "BELUM" -> {
                    tabBelum.setBackgroundResource(R.drawable.bg_tab_active_new)
                    card1.visibility = if (!s1) View.VISIBLE else View.GONE
                    card2.visibility = if (!s2) View.VISIBLE else View.GONE
                    card3.visibility = View.VISIBLE
                }
            }
        }

        tabAll.setOnClickListener { filterCards("ALL") }
        tabSelesai.setOnClickListener { filterCards("SELESAI") }
        tabBelum.setOnClickListener { filterCards("BELUM") }
    }

    override fun onResume() {
        super.onResume()
        loadUI()
    }

    private fun loadUI() {
        val pref = requireActivity().getSharedPreferences("KUIS", Context.MODE_PRIVATE)

        // --- Materi 1 ---
        val s1 = pref.getBoolean("materi1_selesai", false)
        val n1 = pref.getInt("nilai_materi1", 0)
        if (s1) {
            tvStatus1.text = "Status: Selesai (Skor: $n1)"
            btnMateri1.text = "Lihat Hasil"

            btnMateri1.setOnClickListener {
                val fragment = if (n1 == 100) {
                    QuizMenang2Fragment()
                } else {
                    QuizMenang1Fragment()
                }

                val bundle = Bundle()
                bundle.putString("QUIZ_TYPE", "QUIZ1")
                fragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        // --- Materi 2 ---
        val s2 = pref.getBoolean("quiz2_selesai", false)
        val n2 = pref.getInt("quiz2_nilai", 0)
        if (s2) {
            tvStatus2.text = "Status: Selesai (Skor: $n2)"
            btnMateri2.text = "Lihat Hasil"
            btnMateri2.setOnClickListener {
                val frag = if (n2 == 100) QuizMenang2Fragment() else QuizMenang1Fragment()
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, frag).addToBackStack(null).commit()
            }
        } else {
            tvStatus2.text = "Status: Belum Dikerjakan"
            btnMateri2.text = "Kerjakan"
            btnMateri2.setOnClickListener {
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, QuizSoal2Fragment()).addToBackStack(null).commit()
            }
        }

        val selesai3 = pref.getBoolean("quiz3_selesai", false)
        val nilai3 = pref.getInt("quiz3_nilai", 0)

        if (selesai3) {
            tvStatus3.text = "Status: Selesai (Skor: $nilai3)"
            btnMateri3.text = "Lihat Hasil"
            btnMateri3.setBackgroundColor(
                ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
            )

            btnMateri3.setOnClickListener {
                val fragment = if (nilai3 == 100) {
                    QuizMenang2Fragment()
                } else {
                    QuizMenang1Fragment()
                }

                val bundle = Bundle()
                bundle.putString("QUIZ_TYPE", "QUIZ3")
                fragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }

        } else {
            tvStatus3.text = "Status: Belum Dikerjakan"
            btnMateri3.text = "Kerjakan"
            btnMateri3.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.nav_active)
            )

            btnMateri3.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, QuizSoal3Fragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}