package com.faiz.bumiloka

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val tvProfileName = view.findViewById<TextView>(R.id.tvProfileName)
        val btnPengaturan = view.findViewById<LinearLayout>(R.id.btnPengaturan)

        currentUser?.let { user ->
            val rawName = when {
                !user.displayName.isNullOrBlank() -> user.displayName
                !user.email.isNullOrBlank() -> user.email?.substringBefore("@")
                else -> "Bumi Lover"
            }

            val nameToShow = rawName?.split(" ")?.joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            } ?: ""

            tvProfileName.text = nameToShow
        }

        btnPengaturan.setOnClickListener {
            val intent = Intent(requireContext(), PengaturanActivity::class.java)
            startActivity(intent)
        }
    }
}
