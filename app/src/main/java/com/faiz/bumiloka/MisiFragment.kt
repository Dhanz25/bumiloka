package com.faiz.bumiloka

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MisiFragment : Fragment(R.layout.fragment_misi) {

    private var userLevel = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.GONE

        // Binding View
        val tvMisiHeader = view.findViewById<TextView>(R.id.tvMisiHeader)
        val tvMisiSubHeader = view.findViewById<TextView>(R.id.tvMisiSubHeader)
        val tvMisiTitle1 = view.findViewById<TextView>(R.id.tvMisiTitle1)
        val tvMisiDesc1 = view.findViewById<TextView>(R.id.tvMisiDesc1)
        val tvMisiTitle2 = view.findViewById<TextView>(R.id.tvMisiTitle2)
        val tvMisiDesc2 = view.findViewById<TextView>(R.id.tvMisiDesc2)
        val tvMisiTitle3 = view.findViewById<TextView>(R.id.tvMisiTitle3)
        val tvMisiDesc3 = view.findViewById<TextView>(R.id.tvMisiDesc3)

        val btnMulaiMateri = view.findViewById<MaterialButton>(R.id.btnMulaiMateri)
        val btnResetMisi = view.findViewById<MaterialButton>(R.id.btnResetMisi)

        val cardTantangan = view.findViewById<MaterialCardView>(R.id.cardTantangan)
        val iconTantangan = view.findViewById<ImageView>(R.id.iconTantangan)
        val btnTantangan = view.findViewById<MaterialButton>(R.id.btnTantangan)

        val cardSkor = view.findViewById<MaterialCardView>(R.id.cardSkor)
        val iconSkor = view.findViewById<ImageView>(R.id.iconSkor)
        val btnSkor = view.findViewById<MaterialButton>(R.id.btnSkor)

        // Ambil Level User
        LevelHelper.getCurrentLevel(requireContext()) { level ->
            userLevel = level
            updateUIByLevel(
                tvMisiHeader,
                tvMisiSubHeader,
                tvMisiTitle1,
                tvMisiDesc1,
                tvMisiTitle2,
                tvMisiDesc2,
                tvMisiTitle3,
                tvMisiDesc3
            )

            // SharedPreferences
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
            val sharedPref = requireActivity().getSharedPreferences(
                "MISI_${userId}_LEVEL_$userLevel",
                Context.MODE_PRIVATE
            )
            val misi1Selesai = sharedPref.getBoolean("misi1_selesai", false)
            val misi2Selesai = sharedPref.getBoolean("misi2_selesai", false)
            val misi3Selesai = sharedPref.getBoolean("misi3_selesai", false)

            // ================= LOGIKA PROGRESS =================
            if (misi1Selesai) {

                setSelesai(btnMulaiMateri)

                // ❌ nonaktifkan klik
                btnMulaiMateri.isEnabled = false
                btnMulaiMateri.isClickable = false

            } else {

                btnMulaiMateri.isEnabled = true

                btnMulaiMateri.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, JelajahiMateriFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }

            if (misi1Selesai && !misi2Selesai) {
                unlockCard(cardTantangan, iconTantangan, btnTantangan, R.drawable.ic_quiz)
                btnTantangan.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, TantanganDiriFragment())
                        .addToBackStack(null)
                        .commit()
                }
            } else if (misi2Selesai) {
                unlockCard(cardTantangan, iconTantangan, btnTantangan, R.drawable.ic_quiz)
                setSelesai(btnTantangan)
                cardTantangan.isEnabled = false
            } else {
                lockCard(cardTantangan, iconTantangan, btnTantangan)
            }

            if (misi2Selesai && !misi3Selesai) {
                unlockCard(cardSkor, iconSkor, btnSkor, R.drawable.ic_target)
                btnSkor.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MisiRaihSkorFragment())
                        .addToBackStack(null)
                        .commit()
                }
            } else if (misi3Selesai) {
                unlockCard(cardSkor, iconSkor, btnSkor, R.drawable.ic_target)
                setSelesai(btnSkor)
                cardSkor.isEnabled = false
            } else {
                lockCard(cardSkor, iconSkor, btnSkor)
            }
        }

        // ================= NAV =================
//        btnMulaiMateri.setOnClickListener {
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, JelajahiMateriFragment())
//                .addToBackStack(null)
//                .commit()
//        }

        btnResetMisi.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
            // Reset ALL levels for this test button
            for (i in 1..5) {
                requireActivity().getSharedPreferences("KUIS_${userId}_LEVEL_$i", Context.MODE_PRIVATE).edit().clear().apply()
                requireActivity().getSharedPreferences("MISI_${userId}_LEVEL_$i", Context.MODE_PRIVATE).edit().clear().apply()
            }

            LevelHelper.resetProgressPerLevel(requireContext(), userId, 1, 0) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MisiFragment())
                    .commit()
            }
        }

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }

    private fun updateUIByLevel(
        header: TextView, subHeader: TextView,
        t1: TextView, d1: TextView,
        t2: TextView, d2: TextView,
        t3: TextView, d3: TextView
    ) {
        header.text = "MISI $userLevel"
        subHeader.text = if (userLevel == 1) "Level 1 (Benih Kehidupan)" else "Level $userLevel (Penjaga Alam)"
        
        if (userLevel >= 2) {
            t1.text = "Jelajahi Energi"
            d1.text = "Pelajari materi tentang energi terbarukan di level 2."
            
            t2.text = "Tantangan Lanjutan"
            d2.text = "Kerjakan kuis level 2 untuk menguji pengetahuan barumu."
            
            t3.text = "Raih Skor Sempurna"
            d3.text = "Dapatkan skor 100 pada kuis level 2."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav?.visibility = View.VISIBLE
    }

    // ================= HELPER =================
    private fun lockCard(card: MaterialCardView, icon: ImageView, button: MaterialButton) {
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
        card.isEnabled = false
        card.alpha = 0.7f
        icon.setImageResource(R.drawable.lock)
        icon.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white))
        button.isEnabled = false
        button.text = "Terkunci"
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
    }

    private fun unlockCard(card: MaterialCardView, icon: ImageView, button: MaterialButton, originalIcon: Int) {
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        card.isEnabled = true
        card.alpha = 1.0f
        icon.setImageResource(originalIcon)
        icon.clearColorFilter()
        button.isEnabled = true
        button.text = "Mulai"
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.nav_active))
    }

    private fun setSelesai(button: MaterialButton) {

        button.text = "Selesai ✓"

        button.isEnabled = false
        button.isClickable = false
        button.isFocusable = false

        button.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.darker_gray
            )
        )

        // 🔥 hapus listener lama
        button.setOnClickListener(null)
    }
}
