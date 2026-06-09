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

class MisiFragment : Fragment(R.layout.fragment_misi) {

    private var userLevel = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Sembunyikan Bottom Navigation
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

        val tvMisiHeader = view.findViewById<TextView>(R.id.tvMisiHeader)
        val tvMisiSubHeader = view.findViewById<TextView>(R.id.tvMisiSubHeader)
        val tvMisiTitle1 = view.findViewById<TextView>(R.id.tvMisiTitle1)
        val tvMisiDesc1 = view.findViewById<TextView>(R.id.tvMisiDesc1)
        val tvMisiTitle2 = view.findViewById<TextView>(R.id.tvMisiTitle2)
        val tvMisiDesc2 = view.findViewById<TextView>(R.id.tvMisiDesc2)
        val tvMisiTitle3 = view.findViewById<TextView>(R.id.tvMisiTitle3)
        val tvMisiDesc3 = view.findViewById<TextView>(R.id.tvMisiDesc3)

        val btnMulaiMateri = view.findViewById<MaterialButton>(R.id.btnMulaiMateri)
        val cardTantangan = view.findViewById<MaterialCardView>(R.id.cardTantangan)
        val iconTantangan = view.findViewById<ImageView>(R.id.iconTantangan)
        val btnTantangan = view.findViewById<MaterialButton>(R.id.btnTantangan)
        val cardSkor = view.findViewById<MaterialCardView>(R.id.cardSkor)
        val iconSkor = view.findViewById<ImageView>(R.id.iconSkor)
        val btnSkor = view.findViewById<MaterialButton>(R.id.btnSkor)

        LevelHelper.getCurrentLevel(requireContext()) { level ->
            userLevel = level
            updateMisiContent(tvMisiHeader, tvMisiSubHeader, tvMisiTitle1, tvMisiDesc1, tvMisiTitle2, tvMisiDesc2, tvMisiTitle3, tvMisiDesc3)

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
            val sharedPref = requireActivity().getSharedPreferences("MISI_${userId}_LEVEL_$userLevel", Context.MODE_PRIVATE)
            
            val m1 = sharedPref.getBoolean("misi1_selesai", false)
            val m2 = sharedPref.getBoolean("misi2_selesai", false)
            val m3 = sharedPref.getBoolean("misi3_selesai", false)

            // Logika Progress Misi 1
            if (m1) setSelesai(btnMulaiMateri)
            else {
                btnMulaiMateri.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, JelajahiMateriFragment())
                        .addToBackStack(null).commit()
                }
            }

            // Logika Progress Misi 2 (Tantangan Diri)
            if (m1 && !m2) {
                unlockCard(cardTantangan, iconTantangan, btnTantangan, R.drawable.ic_quiz)
                btnTantangan.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, TantanganDiriFragment())
                        .addToBackStack(null).commit()
                }
            } else if (m2) {
                unlockCard(cardTantangan, iconTantangan, btnTantangan, R.drawable.ic_quiz)
                setSelesai(btnTantangan)
            } else lockCard(cardTantangan, iconTantangan, btnTantangan)

            // Logika Progress Misi 3 (Raih Skor)
            if (m2 && !m3) {
                unlockCard(cardSkor, iconSkor, btnSkor, R.drawable.ic_target)
                btnSkor.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MisiRaihSkorFragment())
                        .addToBackStack(null).commit()
                }
            } else if (m3) {
                unlockCard(cardSkor, iconSkor, btnSkor, R.drawable.ic_target)
                setSelesai(btnSkor)
            } else lockCard(cardSkor, iconSkor, btnSkor)
        }

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
        }
    }

    private fun updateMisiContent(header: TextView, sub: TextView, t1: TextView, d1: TextView, t2: TextView, d2: TextView, t3: TextView, d3: TextView) {
        header.text = "MISI LEVEL $userLevel"
        when (userLevel) {
            1 -> {
                sub.text = "Eco Beginner"
                t1.text = "Dasar Lingkungan"
                d1.text = "Pahami cara menjaga bumi dari hal yang paling sederhana."
                t2.text = "Kuis Umum"
                d2.text = "Uji pengetahuan dasarmu tentang kebersihan lingkungan."
                t3.text = "Target Skor"
                d3.text = "Raih skor 75 pada kuis lingkungan umum."
            }
            2 -> {
                sub.text = "Eco Warrior (Fokus Sampah)"
                t1.text = "Master Sampah"
                d1.text = "Pelajari perbedaan sampah organik dan anorganik secara mendalam."
                t2.text = "Kuis Pemilahan"
                d2.text = "Selesaikan tantangan kuis mengenai manajemen sampah dan 3R."
                t3.text = "Ahli Daur Ulang"
                d3.text = "Dapatkan skor minimal 80 pada kuis khusus bertema sampah."
            }
            3 -> {
                sub.text = "Nature Protector (Fokus Air)"
                t1.text = "Penjaga Air"
                d1.text = "Pelajari teknik konservasi air bersih untuk masa depan bumi."
                t2.text = "Kuis Konservasi"
                d2.text = "Selesaikan kuis tentang siklus air dan cara menghematnya."
                t3.text = "Master Hidrologi"
                d3.text = "Buktikan dirimu ahli hemat air dengan skor sempurna di level ini."
            }
        }
    }

    private fun lockCard(card: MaterialCardView, icon: ImageView, button: MaterialButton) {
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
        card.isEnabled = false; card.alpha = 0.7f
        icon.setImageResource(R.drawable.lock)
        button.isEnabled = false; button.text = "Terkunci"
    }

    private fun unlockCard(card: MaterialCardView, icon: ImageView, button: MaterialButton, res: Int) {
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        card.isEnabled = true; card.alpha = 1.0f
        icon.setImageResource(res)
        button.isEnabled = true; button.text = "Mulai"
    }

    private fun setSelesai(button: MaterialButton) {
        button.text = "Selesai ✓"
        button.isEnabled = false
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
    }

    override fun onResume() {
        super.onResume()
        // ✅ Sembunyikan Bottom Navigation saat kembali ke fragment ini
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }
}