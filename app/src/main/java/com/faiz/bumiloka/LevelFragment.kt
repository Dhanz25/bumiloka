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
        LevelHelper.getHighestUnlockedLevel { highest ->
            highestUnlocked = highest
            LevelHelper.getCurrentLevel(requireContext()) { current ->
                activeLevel = current
                setupRecyclerView()
            }
        }
    }

    private fun setupRecyclerView() {
        val levels = listOf(
            LevelModel(1, "Eco Beginner", "Belajar dasar kepedulian lingkungan."),
            LevelModel(2, "Eco Warrior", "Mulai aksi nyata mengelola sampah."),
            LevelModel(3, "Nature Protector", "Menjaga keseimbangan ekosistem."),
            LevelModel(4, "Green Ambassador", "Menjadi inspirasi bagi lingkungan."),
            LevelModel(5, "Earth Savior", "Penyelamat bumi tingkat akhir.")
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
