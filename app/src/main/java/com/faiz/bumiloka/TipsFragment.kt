package com.faiz.bumiloka

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

class TipsFragment : Fragment(R.layout.fragment_tips) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pref = requireActivity().getSharedPreferences("KUIS", Context.MODE_PRIVATE)

        val tips1 = pref.getBoolean("tips_materi1", false)
        val tips2 = pref.getBoolean("tips_materi2", false)
        val tips3 = pref.getBoolean("tips_materi3", false)

        val layoutEmpty = view.findViewById<View>(R.id.layoutEmpty)
        val layoutContent = view.findViewById<View>(R.id.layoutContent)

        val card1 = view.findViewById<View>(R.id.cardTips1)
        val card2 = view.findViewById<View>(R.id.cardTips2)
        val card3 = view.findViewById<View>(R.id.cardTips3)


// 🔥 EMPTY CHECK
        if (!tips1 && !tips2 && !tips3) {
            layoutEmpty.visibility = View.VISIBLE
            layoutContent.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            layoutContent.visibility = View.VISIBLE
        }


// 🔓 TAMPILKAN SESUAI YANG SUDAH UNLOCK
        card1.visibility = if (tips1) View.VISIBLE else View.GONE
        card2.visibility = if (tips2) View.VISIBLE else View.GONE
        card3.visibility = if (tips3) View.VISIBLE else View.GONE


// 🔘 CLICK (hanya aktif kalau visible)
        card1.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TipsPeduliFragment())
                .addToBackStack(null)
                .commit()
        }

        card2.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TipsSampahFragment())
                .addToBackStack(null)
                .commit()
        }

        card3.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TipsHematAirFragment())
                .addToBackStack(null)
                .commit()
        }
        }
    }