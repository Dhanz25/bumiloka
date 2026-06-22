package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.adapters.TantanganBonusAdapter
import com.faiz.bumiloka.model.Tantangan
import com.google.firebase.database.*

class TantanganFragment : Fragment(R.layout.fragment_tantangan) {

    private lateinit var rvBonus: RecyclerView
    private lateinit var bonusAdapter: TantanganBonusAdapter
    private val bonusList = mutableListOf<Tantangan>()
    private val db = FirebaseDatabase.getInstance().reference.child("tantangan")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.GONE

        val btnBack = view.findViewById<View>(R.id.btnBack)
        val btnMulai1 = view.findViewById<Button>(R.id.btnMulai1)
        val btnMulai2 = view.findViewById<Button>(R.id.btnMulai2)
        
        rvBonus = view.findViewById(R.id.rvTantanganBonus)
        rvBonus.layoutManager = LinearLayoutManager(requireContext())

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // =========================
        // PENJELAJAH MINGGUAN
        // ==========================
        if (TantanganStatusHelper.isPenjelajahSelesai(requireContext())) {
            btnMulai1.text = "Selesai ✓"
            btnMulai1.isEnabled = false
        }
        btnMulai1.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TantanganPenjelajahMingguanFragment())
                .addToBackStack(null)
                .commit()
        }

        // =========================
        // MASTER KUIS
        // ==========================
        if (TantanganStatusHelper.isMasterKuisSelesai(requireContext())) {
            btnMulai2.text = "Selesai ✓"
            btnMulai2.isEnabled = false
        }
        btnMulai2.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TantanganMasterKuisFragment())
                .addToBackStack(null)
                .commit()
        }

        // =========================
        // TANTANGAN BONUS (FIREBASE)
        // ==========================
        setupBonusRecyclerView()
        loadTantanganBonus()
    }

    private fun setupBonusRecyclerView() {
        bonusAdapter = TantanganBonusAdapter(bonusList) { tantangan ->
            // Logika "Mulai" Tantangan Bonus
            // Navigasi ke MateriFragment berdasarkan materiId dari Firebase
            val fragment = MateriFragment.newInstance(tantangan.materiId).apply {
                val bundle = arguments ?: Bundle()
                bundle.putString("challenge_id", tantangan.id)
                bundle.putInt("quiz_id", tantangan.quizId)
                bundle.putInt("badge_id", tantangan.badgeId)
                bundle.putBoolean("IS_TANTANGAN_BONUS", true)
                arguments = bundle
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        rvBonus.adapter = bonusAdapter
    }

    private fun loadTantanganBonus() {
        db.orderByChild("aktif").equalTo(true).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                bonusList.clear()
                for (data in snapshot.children) {
                    try {
                        val t = data.getValue(Tantangan::class.java)?.copy(id = data.key ?: "")
                        if (t != null) {
                            bonusList.add(t)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                bonusAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Gagal memuat tantangan bonus", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav.visibility = View.VISIBLE
    }
}