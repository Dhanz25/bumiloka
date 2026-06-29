package com.faiz.bumiloka

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
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
    private lateinit var tvLevelDesc: TextView
    private val bonusList = mutableListOf<Tantangan>()
    private val db = FirebaseDatabase.getInstance().reference.child("tantangan")
    private var userLevel = 1
    private var dbListener: ValueEventListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

        val btnBack = view.findViewById<View>(R.id.btnBack)
        val btnMulai1 = view.findViewById<Button>(R.id.btnMulai1)
        val btnMulai2 = view.findViewById<Button>(R.id.btnMulai2)
        tvLevelDesc = view.findViewById(R.id.tv_level_desc)

        rvBonus = view.findViewById(R.id.rvTantanganBonus)
        rvBonus.layoutManager = LinearLayoutManager(requireContext())
        rvBonus.isNestedScrollingEnabled = false

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupBonusRecyclerView()

        LevelHelper.getCurrentLevel(requireContext()) { level ->
            userLevel = level
            val levelName = getLevelName(level)
            tvLevelDesc.text = "Level $level ($levelName)"
            loadTantanganBonus()
        }

        setupStaticChallenges(btnMulai1, btnMulai2)
    }

    private fun getLevelName(level: Int): String {
        return when (level) {
            1 -> "Benih Kesadaran"
            2 -> "Tunas Kepedulian"
            3 -> "Pohon Kelestarian"
            else -> "Pahlawan Lingkungan"
        }
    }

    private fun setupStaticChallenges(btn1: Button, btn2: Button) {
        // Tantangan 1: Penjelajah Mingguan
        if (TantanganStatusHelper.isPenjelajahSelesai(requireContext())) {
            btn1.text = "Selesai ✓"
            btn1.isEnabled = false
        }
        btn1.setOnClickListener {
            val fragment = DetailTantanganFragment.newInstance(
                id = "penjelajah_mingguan",
                judul = "Penjelajah Mingguan",
                deskripsi = "Selesaikan aktivitas belajar di Level $userLevel dalam 7 hari untuk menuntaskan tantangan ini.",
                imageUrl = "tantangan1",
                materiId = "materi_level_$userLevel", // Ganti dengan ID materi asli
                quizId = "quiz_level_$userLevel"     // Ganti dengan ID kuis asli
            )
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Tantangan 2: Master Kuis (Sesuai Gambar)
        if (TantanganStatusHelper.isMasterKuisSelesai(requireContext())) {
            btn2.text = "Selesai ✓"
            btn2.isEnabled = false
        }
        btn2.setOnClickListener {
            val fragment = DetailTantanganFragment.newInstance(
                id = "master_kuis",
                judul = "Master Kuis",
                deskripsi = "Buktikan kemampuanmu dengan menaklukkan kuis dan raih skor terbaikmu!",
                imageUrl = "tantangan1",
                materiId = "materi_master", // Ganti dengan ID materi asli
                quizId = "quiz_master"     // Ganti dengan ID kuis asli
            )
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupBonusRecyclerView() {
        bonusAdapter = TantanganBonusAdapter(bonusList) { tantangan ->
            // Navigasi ke DetailTantanganFragment untuk tantangan bonus dari Firebase
            val fragment = DetailTantanganFragment.newInstance(
                id = tantangan.id,
                judul = tantangan.judul,
                deskripsi = tantangan.deskripsi,
                imageUrl = tantangan.imageUrl,
                materiId = tantangan.materiId,
                quizId = tantangan.quizId,
                badgeId = tantangan.badgeId
            )

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        rvBonus.adapter = bonusAdapter
    }

    private fun loadTantanganBonus() {
        dbListener?.let { db.removeEventListener(it) }

        dbListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                bonusList.clear()
                
                for (data in snapshot.children) {
                    if (data.value is Map<*, *>) {
                        try {
                            val t = data.getValue(Tantangan::class.java)?.copy(id = data.key ?: "")
                            if (t != null && t.aktif) {
                                if (t.level <= userLevel || t.level == 0) {
                                    bonusList.add(t)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("TantanganFragment", "Gagal parsing: ${e.message}")
                        }
                    }
                }
                
                bonusList.sortByDescending { it.createdAt }
                bonusAdapter.notifyDataSetChanged()
                
                val hasData = bonusList.isNotEmpty()
                val tvTitleBonus = view?.findViewById<TextView>(R.id.tv_title_bonus)
                if (tvTitleBonus != null) {
                    tvTitleBonus.visibility = if (hasData) View.VISIBLE else View.GONE
                    tvTitleBonus.text = "Tantangan Umum"
                }
                
                rvBonus.visibility = if (hasData) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        db.orderByChild("aktif").equalTo(true).addValueEventListener(dbListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbListener?.let { db.removeEventListener(it) }
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
    }
}