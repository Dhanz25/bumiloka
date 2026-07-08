package com.faiz.bumiloka

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.model.Edukasi
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MisiFragment : Fragment(R.layout.fragment_misi) {

    private var userLevel = 1
    private var firstEdukasiId: String? = null
    private var firstQuizId: String? = null
    private var isMateriFetched = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
        
        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        loadMisiData()
    }

    override fun onResume() {
        super.onResume()
        loadMisiData()
    }

    private fun loadMisiData() {
        if (!isAdded) return
        val view = view ?: return
        
        val pbOverall = view.findViewById<ProgressBar>(R.id.pbOverallMisi)
        val tvProgressText = view.findViewById<TextView>(R.id.tvProgressText)
        val tvMisiSubHeader = view.findViewById<TextView>(R.id.tvMisiSubHeader)
        val tvMisiHeader = view.findViewById<TextView>(R.id.tvMisiHeader)

        LevelHelper.getCurrentLevel(requireContext()) { level ->
            if (!isAdded) return@getCurrentLevel
            userLevel = level
            tvMisiHeader.text = "MISI LEVEL $userLevel"
            tvMisiSubHeader.text = getLevelName(level)

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
            val sharedPref = requireActivity().getSharedPreferences("MISI_${userId}_LEVEL_$level", Context.MODE_PRIVATE)
            val kuisPref = requireActivity().getSharedPreferences("KUIS_${userId}_LEVEL_$level", Context.MODE_PRIVATE)
            
            val q1Id = (3 * (level - 1) + 1).toString()
            val q3Id = (3 * (level - 1) + 3).toString()

            val m1 = sharedPref.getBoolean("misi1_selesai", false) || 
                     kuisPref.getBoolean("materi1_selesai", false) ||
                     kuisPref.getBoolean("quiz1_selesai", false) ||
                     kuisPref.getBoolean("kuis_${q1Id}_selesai", false)

            val m2 = sharedPref.getBoolean("misi2_selesai", false) || 
                     kuisPref.getBoolean("quiz1_selesai", false) || 
                     kuisPref.getBoolean("kuis_${q1Id}_selesai", false) ||
                     kuisPref.getInt("quiz1_nilai", 0) > 0

            val m3 = sharedPref.getBoolean("misi3_selesai", false) || 
                     kuisPref.getInt("quiz3_nilai", 0) >= 75 ||
                     kuisPref.getInt("kuis_${q3Id}_skor", 0) >= 75

            updateUI(view, m1, m2, m3)

            val totalSelesai = listOf(m1, m2, m3).count { it }
            pbOverall.progress = (totalSelesai * 33.3).toInt()
            tvProgressText.text = "$totalSelesai dari 3 Misi Selesai"

            if (!isMateriFetched) {
                fetchFirstMateri(level, view.findViewById(R.id.tvMisiTitle1), view.findViewById(R.id.tvMisiDesc1))
                isMateriFetched = true
            }
        }
    }

    private fun updateUI(view: View, m1: Boolean, m2: Boolean, m3: Boolean) {
        val btn1 = view.findViewById<MaterialButton>(R.id.btnMulaiMateri)
        val btn2 = view.findViewById<MaterialButton>(R.id.btnTantangan)
        val btn3 = view.findViewById<MaterialButton>(R.id.btnSkor)
        
        val step1 = view.findViewById<TextView>(R.id.tvStepCircle1)
        val step2 = view.findViewById<TextView>(R.id.tvStepCircle2)
        val step3 = view.findViewById<TextView>(R.id.tvStepCircle3)

        if (m1) setSelesaiStyle(btn1, step1) else unlockCardStyle(view.findViewById(R.id.cardMateri), btn1, step1)
        
        if (m1) {
            if (m2) setSelesaiStyle(btn2, step2) else unlockCardStyle(view.findViewById(R.id.cardTantangan), btn2, step2)
        } else {
            lockCardStyle(view.findViewById(R.id.cardTantangan), btn2, step2)
        }

        if (m1 && m2) {
            if (m3) setSelesaiStyle(btn3, step3) else unlockCardStyle(view.findViewById(R.id.cardSkor), btn3, step3)
        } else {
            lockCardStyle(view.findViewById(R.id.cardSkor), btn3, step3)
        }
        
        btn1.setOnClickListener { openMateri() }
        btn2.setOnClickListener { 
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, TantanganDiriFragment()).addToBackStack(null).commit() 
        }
        btn3.setOnClickListener { 
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, MisiRaihSkorFragment()).addToBackStack(null).commit() 
        }
    }

    private fun openMateri() {
        val fragment = MateriFragment()
        fragment.arguments = Bundle().apply {
            putString("edukasi_id", firstEdukasiId ?: "")
            putString("quiz_id", firstQuizId ?: "")
            putInt("LEVEL", userLevel) // PASS LEVEL KE MATERI
        }
        parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
    }

    private fun fetchFirstMateri(level: Int, titleView: TextView, descView: TextView) {
        FirebaseDatabase.getInstance().reference.child("edukasi").orderByChild("level").equalTo(level.toDouble()).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    snapshot.children.firstOrNull()?.let { child ->
                        val edukasi = child.getValue(Edukasi::class.java)
                        firstEdukasiId = child.key
                        firstQuizId = edukasi?.kuisId
                        titleView.text = edukasi?.title ?: "Materi Level $level"
                        descView.text = edukasi?.description ?: "Pelajari materi untuk menyelesaikan misi."
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getLevelName(level: Int) = when (level) {
        1 -> "Benih Kesadaran"
        2 -> "Tunas Kepedulian"
        3 -> "Pohon Kelestarian"
        else -> "Pahlawan Lingkungan"
    }

    private fun lockCardStyle(card: MaterialCardView, button: MaterialButton, stepCircle: TextView) {
        card.alpha = 0.5f
        button.isEnabled = false
        button.text = "Terkunci"
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey_button))
        stepCircle.setBackgroundResource(R.drawable.bg_option)
    }

    private fun unlockCardStyle(card: MaterialCardView, button: MaterialButton, stepCircle: TextView) {
        card.alpha = 1.0f
        button.isEnabled = true
        button.text = "Mulai"
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_green))
        stepCircle.setBackgroundResource(R.drawable.bg_badge_circle)
    }

    private fun setSelesaiStyle(button: MaterialButton, stepCircle: TextView) {
        button.text = "Selesai ✓"
        button.isEnabled = false
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_green))
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_green))
        stepCircle.setBackgroundResource(R.drawable.bg_circle_green)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
    }
}
