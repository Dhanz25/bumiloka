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

        // ❌ Sembunyikan Bottom Navigation
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

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

        LevelHelper.getCurrentLevel(requireContext()) { level ->
            userLevel = level
            val levelName = when (level) {
                1 -> "Eco Beginner"
                2 -> "Eco Warrior"
                3 -> "Nature Protector"
                else -> "Eco Beginner"
            }
            tvEdukasiLevel.text = "Level $level ($levelName)"
            
            when (level) {
                1 -> {
                    tvTitle1.text = "Peduli Lingkungan"
                    img1.setImageResource(R.drawable.img_lingkungan)
                    tvTitle2.text = "Kelola Sampah"
                    img2.setImageResource(R.drawable.img_sampah)
                    tvTitle3.text = "Hemat Air"
                    img3.setImageResource(R.drawable.img_air)
                }
                2 -> {
                    // Fokus SAMPAH
                    tvTitle1.text = "Jenis Sampah"
                    img1.setImageResource(R.drawable.img_sampah)
                    tvTitle2.text = "Konsep 3R"
                    img2.setImageResource(R.drawable.img_sampah)
                    tvTitle3.text = "Bahaya Plastik"
                    img3.setImageResource(R.drawable.img_sampah)
                }
                3 -> {
                    // Fokus HEMAT AIR
                    tvTitle1.text = "Konservasi Air"
                    img1.setImageResource(R.drawable.img_air)
                    tvTitle2.text = "Siklus Air"
                    img2.setImageResource(R.drawable.img_air)
                    tvTitle3.text = "Teknik Hemat Air"
                    img3.setImageResource(R.drawable.img_air)
                }
            }
        }

        view.findViewById<View?>(R.id.materi1)?.setOnClickListener {
            bukaMateri(1)
        }

        view.findViewById<View?>(R.id.materi2)?.setOnClickListener {
            bukaMateri(2)
        }

        view.findViewById<View?>(R.id.materi3)?.setOnClickListener {
            bukaMateri(3)
        }
    }

    private fun bukaMateri(index: Int) {
        val dariTantangan = arguments?.getBoolean("DARI_TANTANGAN", false) ?: false
        val fragment = MateriFragment.newInstance(index)
        val args = fragment.arguments ?: Bundle()
        args.putBoolean("DARI_TANTANGAN", dariTantangan)
        fragment.arguments = args
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    override fun onResume() {
        super.onResume()
        // ❌ Tetap Sembunyikan Bottom Navigation saat kembali ke fragment ini
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 🔺 Tampilkan kembali Bottom Navigation saat keluar fragment
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
    }
}