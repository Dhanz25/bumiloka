package com.faiz.bumiloka

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.model.Edukasi
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MisiFragment : Fragment(R.layout.fragment_misi) {

    private var userLevel = 1
    private var firstEdukasiId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

        val tvMisiHeader = view.findViewById<TextView>(R.id.tvMisiHeader)
        val tvMisiSubHeader = view.findViewById<TextView>(R.id.tvMisiSubHeader)
        val tvMisiTitle1 = view.findViewById<TextView>(R.id.tvMisiTitle1)
        val tvMisiDesc1 = view.findViewById<TextView>(R.id.tvMisiDesc1)
        
        val btnMulaiMateri = view.findViewById<MaterialButton>(R.id.btnMulaiMateri)
        val cardTantangan = view.findViewById<MaterialCardView>(R.id.cardTantangan)
        val iconTantangan = view.findViewById<ImageView>(R.id.iconTantangan)
        val btnTantangan = view.findViewById<MaterialButton>(R.id.btnTantangan)
        val cardSkor = view.findViewById<MaterialCardView>(R.id.cardSkor)
        val iconSkor = view.findViewById<ImageView>(R.id.iconSkor)
        val btnSkor = view.findViewById<MaterialButton>(R.id.btnSkor)

        LevelHelper.getCurrentLevel(requireContext()) { level ->
            userLevel = level
            tvMisiHeader.text = "MISI LEVEL $userLevel"
            
            val levelName = when (level) {
                1 -> "Eco Beginner"
                2 -> "Eco Warrior"
                3 -> "Nature Protector"
                else -> "Eco Beginner"
            }
            tvMisiSubHeader.text = levelName

            // Ambil Judul Misi 1 secara dinamis dari Firebase
            fetchFirstMateri(level, tvMisiTitle1, tvMisiDesc1)

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
            val sharedPref = requireActivity().getSharedPreferences("MISI_${userId}_LEVEL_$userLevel", Context.MODE_PRIVATE)
            
            val m1 = sharedPref.getBoolean("misi1_selesai", false)
            val m2 = sharedPref.getBoolean("misi2_selesai", false)
            val m3 = sharedPref.getBoolean("misi3_selesai", false)

            if (m1) setSelesai(btnMulaiMateri)
            else {
                btnMulaiMateri.setOnClickListener {
                    val fragment = JelajahiMateriFragment()
                    val args = Bundle()
                    args.putString("edukasi_id", firstEdukasiId)
                    fragment.arguments = args
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null).commit()
                }
            }

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

    private fun fetchFirstMateri(level: Int, titleView: TextView, descView: TextView) {
        val db = FirebaseDatabase.getInstance().reference.child("edukasi")
        db.orderByChild("level").equalTo(level.toDouble()).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val child = snapshot.children.firstOrNull()
                    val edukasi = child?.getValue(Edukasi::class.java)
                    if (edukasi != null) {
                        firstEdukasiId = child.key
                        titleView.text = edukasi.title
                        descView.text = edukasi.description
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
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
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }
}
