package com.faiz.bumiloka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class EdukasiFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edukasi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
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

    // 🔥 NAVIGASI KE MATERI (FIX)
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

    // 🔻 Hide Bottom Nav
    override fun onResume() {
        super.onResume()
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav?.visibility = View.GONE
    }

    // 🔺 Show Bottom Nav
    override fun onPause() {
        super.onPause()
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        bottomNav?.visibility = View.VISIBLE
    }
}