package com.faiz.bumiloka

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth

class MisiFragment : Fragment(R.layout.fragment_misi) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.GONE

        // Binding View
        val btnMulaiMateri = view.findViewById<MaterialButton>(R.id.btnMulaiMateri)

        //Reset Misi
//        val btnResetMisi = view.findViewById<MaterialButton>(R.id.btnResetMisi)

        val cardTantangan = view.findViewById<MaterialCardView>(R.id.cardTantangan)
        val iconTantangan = view.findViewById<ImageView>(R.id.iconTantangan)
        val btnTantangan = view.findViewById<MaterialButton>(R.id.btnTantangan)

        val cardSkor = view.findViewById<MaterialCardView>(R.id.cardSkor)
        val iconSkor = view.findViewById<ImageView>(R.id.iconSkor)
        val btnSkor = view.findViewById<MaterialButton>(R.id.btnSkor)

        // SharedPreferences
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val sharedPref = requireActivity().getSharedPreferences("MISI_$userId", Context.MODE_PRIVATE)
        val misi1Selesai = sharedPref.getBoolean("misi1_selesai", false)
        val misi2Selesai = sharedPref.getBoolean("misi2_selesai", false)
        val misi3Selesai = sharedPref.getBoolean("misi3_selesai", false)

        // ================= LOGIKA =================

        // Misi 1
        if (misi1Selesai) {
            setSelesai(btnMulaiMateri)
        }

        // Misi 2
        if (misi1Selesai && !misi2Selesai) {
            unlockCard(cardTantangan, iconTantangan, btnTantangan, R.drawable.ic_quiz)

            btnTantangan.setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TantanganDiriFragment())
                    .addToBackStack(null)
                    .commit()
            }

        } else if (misi2Selesai) {
            unlockCard(cardTantangan, iconTantangan, btnTantangan, R.drawable.ic_quiz)
            setSelesai(btnTantangan)
            cardTantangan.isEnabled = false
            btnTantangan.setOnClickListener(null)

        } else {
            lockCard(cardTantangan, iconTantangan, btnTantangan)
        }

        // Misi 3 (SKOR)
        if (misi2Selesai && !misi3Selesai) {

            unlockCard(cardSkor, iconSkor, btnSkor, R.drawable.ic_target)

            btnSkor.setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MisiRaihSkorFragment())
                    .addToBackStack(null)
                    .commit()
            }

        } else if (misi3Selesai) {

            unlockCard(cardSkor, iconSkor, btnSkor, R.drawable.ic_target)
            setSelesai(btnSkor)

            cardSkor.isEnabled = false
            btnSkor.isEnabled = false
            btnSkor.setOnClickListener(null)

        } else {
            lockCard(cardSkor, iconSkor, btnSkor)
        }

        // ================= NAV =================

        btnMulaiMateri.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, JelajahiMateriFragment())
                .addToBackStack(null)
                .commit()
        }

//        btnResetMisi.setOnClickListener {
//
//        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
//
//        val prefMisi = requireActivity()
//            .getSharedPreferences("MISI_$userId", Context.MODE_PRIVATE)
//
//        val prefKuis = requireActivity()
//            .getSharedPreferences("KUIS_$userId", Context.MODE_PRIVATE)
//
//        // 🔥 Hapus semua data misi
//        prefMisi.edit().clear().apply()
//
//        // 🔥 Hapus semua data kuis
//        prefKuis.edit().clear().apply()
//
//        // 🔥 Reload fragment
//        requireActivity().supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, MisiFragment())
//            .commit()
//    }

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 🔺 Tampilkan kembali Bottom Navigation saat keluar fragment
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.VISIBLE
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
        button.setBackgroundColor(
            ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        )
    }
}