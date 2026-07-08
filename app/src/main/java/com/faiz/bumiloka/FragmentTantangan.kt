package com.faiz.bumiloka

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.adapters.TantanganBonusAdapter
import com.faiz.bumiloka.model.Tantangan
import com.google.firebase.database.*

class TantanganFragment : Fragment(R.layout.fragment_tantangan) {

    private lateinit var rvTantangan: RecyclerView
    private lateinit var adapter: TantanganBonusAdapter
    private lateinit var tvLevelDesc: TextView
    private lateinit var progressBar: ProgressBar
    
    private val tantanganList = mutableListOf<Tantangan>()
    private val db = FirebaseDatabase.getInstance().reference.child("tantangan")
    private var currentLevel = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setBottomNavigationVisibility(false)

        val btnBack = view.findViewById<View>(R.id.btnBack)
        tvLevelDesc = view.findViewById(R.id.tv_level_desc)
        progressBar = view.findViewById(R.id.progress_tantangan)

        rvTantangan = view.findViewById(R.id.rvTantanganBonus)
        rvTantangan.layoutManager = LinearLayoutManager(requireContext())
        rvTantangan.isNestedScrollingEnabled = false

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        setupRecyclerView()
        
        LevelHelper.getCurrentLevel(requireContext()) { level ->
            if (!isAdded) return@getCurrentLevel
            
            tvLevelDesc.text = "Level $level (${getLevelName(level)})"
            
            // Jika kembali dari detail (level sama & data sudah ada), langsung tampilkan
            if (level == currentLevel && tantanganList.isNotEmpty()) {
                rvTantangan.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                return@getCurrentLevel
            }

            currentLevel = level
            loadTantanganUtama(level)
        }
    }

    private fun setupRecyclerView() {
        adapter = TantanganBonusAdapter(tantanganList) { tantangan ->
            val fragment = DetailTantanganFragment.newInstance(tantangan)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        rvTantangan.adapter = adapter
        
        if (tantanganList.isNotEmpty()) {
            rvTantangan.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }

    private fun loadTantanganUtama(level: Int) {
        if (!isAdded) return
        
        if (tantanganList.isEmpty()) {
            progressBar.visibility = View.VISIBLE
            rvTantangan.visibility = View.GONE
        }
        
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                
                val freshList = mutableListOf<Tantangan>()
                for (data in snapshot.children) {
                    try {
                        val map = data.value as? Map<*, *> ?: continue
                        
                        // Konversi manual untuk menghindari crash "Long to String"
                        val tId = data.key ?: ""
                        val tLevel = when (val lv = map["level"]) {
                            is Long -> lv.toInt()
                            is String -> lv.toIntOrNull() ?: 1
                            else -> 1
                        }
                        val isAktif = map["aktif"] as? Boolean ?: true

                        if (isAktif && tLevel == level) {
                            val tantangan = Tantangan(
                                id = tId,
                                judul = map["judul"]?.toString() ?: "",
                                deskripsi = map["deskripsi"]?.toString() ?: "",
                                imageUrl = map["imageUrl"]?.toString() ?: "",
                                materiId = map["materiId"]?.toString() ?: "",
                                quizId = map["quizId"]?.toString() ?: "",
                                type = map["type"]?.toString() ?: "SINGLE",
                                targetCount = (map["targetCount"] as? Long)?.toInt() ?: 1,
                                badgeId = map["badgeId"]?.toString() ?: "",
                                level = tLevel,
                                aktif = isAktif,
                                createdAt = (map["createdAt"] as? Long) ?: 0L
                            )
                            freshList.add(tantangan)
                        }
                    } catch (e: Exception) {
                        Log.e("TantanganDEBUG", "Error parsing: ${data.key} -> ${e.message}")
                    }
                }
                
                freshList.sortByDescending { it.createdAt }
                
                tantanganList.clear()
                tantanganList.addAll(freshList)
                adapter.notifyDataSetChanged()
                
                progressBar.visibility = View.GONE
                rvTantangan.visibility = if (tantanganList.isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Gagal memuat tantangan", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getLevelName(level: Int) = when (level) {
        1 -> "Benih Kesadaran"
        2 -> "Tunas Kepedulian"
        3 -> "Pohon Kelestarian"
        else -> "Pahlawan Lingkungan"
    }
}
