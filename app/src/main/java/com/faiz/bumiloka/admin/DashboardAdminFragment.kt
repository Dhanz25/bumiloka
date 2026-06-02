package com.faiz.bumiloka.admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.R
import com.faiz.bumiloka.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class DashboardAdminFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_dashboard_admin,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()

        val tvAdminName =
            view.findViewById<TextView>(R.id.tvAdminName)

        val cardEdukasi =
            view.findViewById<CardView>(R.id.cardEdukasi)

        val cardKuis =
            view.findViewById<CardView>(R.id.cardKuis)

        val cardTantangan =
            view.findViewById<CardView>(R.id.cardTantangan)

        val cardStatistik =
            view.findViewById<CardView>(R.id.cardStatistik)

        val btnLogout =
            view.findViewById<ImageView>(R.id.btnLogout)

        // NAMA ADMIN

        val currentUser = auth.currentUser

        val adminName =
            currentUser?.displayName ?: "Admin"

        tvAdminName.text =
            "Halo, $adminName 👋"

        // EDUKASI

        cardEdukasi.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    EdukasiFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        // KUIS

        cardKuis.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    KuisFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        // TANTANGAN

        cardTantangan.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    TantanganFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        // STATISTIK

        cardStatistik.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    StatistikFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        // LOGOUT

        btnLogout.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar dari admin?")
                .setPositiveButton("Ya") { _, _ ->

                    auth.signOut()

                    startActivity(
                        Intent(
                            requireContext(),
                            LoginActivity::class.java
                        )
                    )

                    requireActivity().finish()
                }

                .setNegativeButton("Batal", null)
                .show()
        }

        return view
    }
}