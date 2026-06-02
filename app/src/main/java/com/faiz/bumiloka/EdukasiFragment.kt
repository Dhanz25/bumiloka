package com.faiz.bumiloka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class EdukasiFragment : Fragment() {

    private var userLevel = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edukasi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        val tvEdukasiLevel = view.findViewById<TextView>(R.id.tvEdukasiLevel)
        
        val tvTitle1 = view.findViewById<TextView>(R.id.tvEdukasiTitle1)
        val img1 = view.findViewById<ImageView>(R.id.imgEdukasi1)
        
        val tvTitle2 = view.findViewById<TextView>(R.id.tvEdukasiTitle2)
        val img2 = view.findViewById<ImageView>(R.id.imgEdukasi2)
        
        val tvTitle3 = view.findViewById<TextView>(R.id.tvEdukasiTitle3)
        val img3 = view.findViewById<ImageView>(R.id.imgEdukasi3)

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Ambil Level User
        LevelHelper.getCurrentLevel { level ->
            userLevel = level
            tvEdukasiLevel.text = "Level $level (${if (level == 1) "Eco Beginner" else "Eco Warrior"})"
            
            if (level >= 2) {
                tvTitle1.text = "Energi Terbarukan"
                img1.setImageResource(R.drawable.img) // Placeholder
                
                tvTitle2.text = "Pemanasan Global"
                img2.setImageResource(R.drawable.img) // Placeholder
                
                tvTitle3.text = "Ekosistem Laut"
                img3.setImageResource(R.drawable.img) // Placeholder
            }
        }

        view.findViewById<View?>(R.id.materi1)?.setOnClickListener {
            bukaMateri(if (userLevel == 1) 1 else 4)
        }

        view.findViewById<View?>(R.id.materi2)?.setOnClickListener {
            bukaMateri(if (userLevel == 1) 2 else 5)
        }

        view.findViewById<View?>(R.id.materi3)?.setOnClickListener {
            bukaMateri(if (userLevel == 1) 3 else 6)
        }
    }

    private fun bukaMateri(id: Int) {
        try {
            val fragment = MateriFragment.newInstance(id)
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, fragment)
                ?.addToBackStack(null)
                ?.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav?.visibility = View.VISIBLE
    }
}
