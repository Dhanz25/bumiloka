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

        val card1 = view.findViewById<View>(R.id.cardTips1)
        val card2 = view.findViewById<View>(R.id.cardTips2)
        val card3 = view.findViewById<View>(R.id.cardTips3)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        // tampil / sembunyi
        card1.visibility = if (tips1) View.VISIBLE else View.GONE
        card2.visibility = if (tips2) View.VISIBLE else View.GONE
        card3.visibility = if (tips3) View.VISIBLE else View.GONE

        // empty state
        if (!tips1 && !tips2 && !tips3) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }

        // klik card
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
                .replace(R.id.fragment_container, TipsFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}