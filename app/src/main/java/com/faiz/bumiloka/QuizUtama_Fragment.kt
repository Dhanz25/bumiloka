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
import com.google.firebase.database.FirebaseDatabase

import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class QuizUtamaFragment : Fragment(R.layout.fragment_quiz_utama_) {

    private lateinit var btnMateri1: Button
    private lateinit var tvStatus1: TextView
    private lateinit var tvTitle1: TextView
    private lateinit var imgQuiz1: ImageView
    
    private lateinit var btnMateri2: Button
    private lateinit var tvStatus2: TextView
    private lateinit var tvTitle2: TextView
    private lateinit var imgQuiz2: ImageView
    
    private lateinit var btnMateri3: Button
    private lateinit var tvStatus3: TextView
    private lateinit var tvTitle3: TextView
    private lateinit var imgQuiz3: ImageView

    private lateinit var btnTips1: Button
    private lateinit var btnTips2: Button
    private lateinit var btnTips3: Button
    
    private lateinit var tvLevelIndicator: TextView

    private var userLevel = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.GONE

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        tvLevelIndicator = view.findViewById(R.id.tvLevelIndicator)

        btnMateri1 = view.findViewById(R.id.btn_detail_materi1)
        tvStatus1 = view.findViewById(R.id.tvStatus1)
        tvTitle1 = view.findViewById(R.id.tvTitle1)
        imgQuiz1 = view.findViewById(R.id.imgQuiz1)

        btnMateri2 = view.findViewById(R.id.btn_kerjakan_materi2)
        tvStatus2 = view.findViewById(R.id.tvStatus2)
        tvTitle2 = view.findViewById(R.id.tvTitle2)
        imgQuiz2 = view.findViewById(R.id.imgQuiz2)

        btnMateri3 = view.findViewById(R.id.btn_kerjakan_materi3)
        tvStatus3 = view.findViewById(R.id.tvStatus3)
        tvTitle3 = view.findViewById(R.id.tvTitle3)
        imgQuiz3 = view.findViewById(R.id.imgQuiz3)

        btnTips1 = view.findViewById(R.id.btnTips1)
        btnTips2 = view.findViewById(R.id.btnTips2)
        btnTips3 = view.findViewById(R.id.btnTips3)

        val tabAll = view.findViewById<TextView>(R.id.tab_all)
        val tabSelesai = view.findViewById<TextView>(R.id.tab_selesai)
        val tabBelum = view.findViewById<TextView>(R.id.tab_belum)

        val card1 = view.findViewById<MaterialCardView>(R.id.card1)
        val card2 = view.findViewById<MaterialCardView>(R.id.card2)
        val card3 = view.findViewById<MaterialCardView>(R.id.card3)

        // Ambil Level User
        LevelHelper.getCurrentLevel { level ->
            userLevel = level
            tvLevelIndicator.text = "Level $level (${if (level == 1) "Eco Beginner" else "Eco Warrior"})"
            loadUI()
        }

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        fun resetTab() {
            tabAll.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            tabSelesai.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            tabBelum.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        }

        fun filterCards(type: String) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
            val pref = requireActivity().getSharedPreferences("KUIS_${userId}_LEVEL_$userLevel", Context.MODE_PRIVATE)
            val s1 = pref.getBoolean("materi1_selesai", false)
            val s2 = pref.getBoolean("quiz2_selesai", false)
            val s3 = pref.getBoolean("quiz3_selesai", false)

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
                    card3.visibility = if (s3) View.VISIBLE else View.GONE
                }
                "BELUM" -> {
                    tabBelum.setBackgroundResource(R.drawable.bg_tab_active_new)
                    card1.visibility = if (!s1) View.VISIBLE else View.GONE
                    card2.visibility = if (!s2) View.VISIBLE else View.GONE
                    card3.visibility = if (!s3) View.VISIBLE else View.GONE
                }
            }
        }

        tabAll.setOnClickListener { filterCards("ALL") }
        tabSelesai.setOnClickListener { filterCards("SELESAI") }
        tabBelum.setOnClickListener { filterCards("BELUM") }
    }

    override fun onResume() {
        super.onResume()
        LevelHelper.getCurrentLevel { level ->
            userLevel = level
            loadUI()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.VISIBLE
    }

    private fun loadUI() {
        val currentLevel = LevelHelper.getCurrentLevelLocal(requireContext())
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val pref = requireActivity().getSharedPreferences("KUIS_${userId}_LEVEL_$userLevel", Context.MODE_PRIVATE)

        // Update Title & Image berdasarkan Level dari Helper
        tvTitle1.text = LevelHelper.getQuizTitle(userLevel, 1)
        imgQuiz1.setImageResource(LevelHelper.getQuizImage(userLevel, 1))
        
        tvTitle2.text = LevelHelper.getQuizTitle(userLevel, 2)
        imgQuiz2.setImageResource(LevelHelper.getQuizImage(userLevel, 2))
        
        tvTitle3.text = LevelHelper.getQuizTitle(userLevel, 3)
        imgQuiz3.setImageResource(LevelHelper.getQuizImage(userLevel, 3))

        // ================= MATERI 1 =================
        val s1 = pref.getBoolean("materi1_selesai", false)
        val n1 = pref.getInt("nilai_materi1", 0)

        if (s1) {
            tvStatus1.text = "Status: Selesai (Skor: $n1)"
            btnMateri1.text = "Lihat Hasil"
            btnMateri1.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))

            btnMateri1.setOnClickListener {
                val fragment = QuizMenang1Fragment()
                val bundle = Bundle()
                bundle.putString("QUIZ_TYPE", "QUIZ1")
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            if (n1 == 100) {
                btnTips1.visibility = View.VISIBLE
                btnTips1.setOnClickListener {
                    pref.edit().putBoolean("tips_materi1", true).apply()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, if (userLevel == 1) TipsPeduliFragment() else TipsFragment())
                        .addToBackStack(null)
                        .commit()
                }
            } else {
                btnTips1.visibility = View.GONE
            }

        } else {
            tvStatus1.text = "Status: Belum Dikerjakan"
            btnMateri1.text = "Kerjakan"
            btnMateri1.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.nav_active))

            btnMateri1.setOnClickListener {
                LevelHelper.openQuizFragment(userLevel, 1, parentFragmentManager)
            }
            btnTips1.visibility = View.GONE
        }

        // ================= MATERI 2 =================
        val s2 = pref.getBoolean("quiz2_selesai", false)
        val n2 = pref.getInt("quiz2_nilai", 0)

        if (s2) {
            tvStatus2.text = "Status: Selesai (Skor: $n2)"
            btnMateri2.text = "Lihat Hasil"
            btnMateri2.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))

            btnMateri2.setOnClickListener {
                val fragment = QuizMenang1Fragment()
                val bundle = Bundle()
                bundle.putString("QUIZ_TYPE", "QUIZ2")
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            if (n2 == 100) {
                btnTips2.visibility = View.VISIBLE
                btnTips2.setOnClickListener {
                    pref.edit().putBoolean("tips_materi2", true).apply()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, if (userLevel == 1) TipsSampahFragment() else TipsFragment())
                        .addToBackStack(null)
                        .commit()
                }
            } else {
                btnTips2.visibility = View.GONE
            }

        } else {
            tvStatus2.text = "Status: Belum Dikerjakan"
            btnMateri2.text = "Kerjakan"
            btnMateri2.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.nav_active))

            btnMateri2.setOnClickListener {
                LevelHelper.openQuizFragment(userLevel, 2, parentFragmentManager)
            }
            btnTips2.visibility = View.GONE
        }

        // ================= MATERI 3 =================
        val s3 = pref.getBoolean("quiz3_selesai", false)
        val n3 = pref.getInt("quiz3_nilai", 0)

        if (s3) {
            tvStatus3.text = "Status: Selesai (Skor: $n3)"
            btnMateri3.text = "Lihat Hasil"
            btnMateri3.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))

            btnMateri3.setOnClickListener {
                val fragment = QuizMenang1Fragment()
                val bundle = Bundle()
                bundle.putString("QUIZ_TYPE", "QUIZ3")
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            if (n3 == 100) {
                btnTips3.visibility = View.VISIBLE
                btnTips3.setOnClickListener {
                    pref.edit().putBoolean("tips_materi3", true).apply()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, if (userLevel == 1) TipsHematAirFragment() else TipsFragment())
                        .addToBackStack(null)
                        .commit()
                }
            } else {
                btnTips3.visibility = View.GONE
            }

        } else {
            tvStatus3.text = "Status: Belum Dikerjakan"
            btnMateri3.text = "Kerjakan"
            btnMateri3.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.nav_active))

            btnMateri3.setOnClickListener {
                LevelHelper.openQuizFragment(userLevel, 3, parentFragmentManager)
            }
            btnTips3.visibility = View.GONE
        }
    }
}
