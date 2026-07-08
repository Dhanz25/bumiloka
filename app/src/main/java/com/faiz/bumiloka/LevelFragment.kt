package com.faiz.bumiloka

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class LevelFragment : Fragment(R.layout.fragment_level) {

    private lateinit var rvLevel: RecyclerView
    private var highestUnlocked = 1
    private var activeLevel = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        rvLevel = view.findViewById(R.id.rvLevel)

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        loadLevels()
    }

    private fun loadLevels() {
        LevelHelper.getHighestUnlockedLevel { highest: Int ->
            // Pastikan highestUnlocked maksimal 3
            highestUnlocked = if (highest > 3) 3 else highest
            LevelHelper.getCurrentLevel(requireContext()) { current: Int ->
                activeLevel = current
                setupRecyclerView()
            }
        }
    }

    private fun setupRecyclerView() {
        // Hanya tampil 3 level
        val levels = listOf(
            LevelModel(1, "Peduli Lingkungan", "Belajar dasar kepedulian lingkungan."),
            LevelModel(2, "Tentang Sampah", "Mulai aksi nyata mengelola sampah."),
            LevelModel(3, "Hemat Air", "Menjaga keseimbangan ekosistem.")
        )

        levels.forEach {
            it.isUnlocked = it.level <= highestUnlocked
            it.isActive = it.level == activeLevel
        }

        rvLevel.layoutManager = LinearLayoutManager(requireContext())
        rvLevel.adapter = LevelAdapter(levels) { selected ->
            if (selected.isUnlocked) {

                LevelHelper.saveSelectedLevel(
                    requireContext(),
                    selected.level
                ) {

                    Toast.makeText(
                        requireContext(),
                        "Berhasil beralih ke Level ${selected.level}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // kembali ke home
                    parentFragmentManager.popBackStack()
                }

            } else {

                Toast.makeText(
                    requireContext(),
                    "Selesaikan level sebelumnya untuk membuka!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
