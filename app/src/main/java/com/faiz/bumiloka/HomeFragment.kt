package com.faiz.bumiloka

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.ui.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)
        val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)
        val btnEdukasi = view.findViewById<CardView>(R.id.btnEdukasi)
        val btnMisi = view.findViewById<CardView>(R.id.btnMisi) // ✅ TAMBAHAN

        fun updateUserName(user: FirebaseUser?) {
            val rawName = when {
                !user?.displayName.isNullOrBlank() -> user?.displayName
                !user?.email.isNullOrBlank() -> user?.email?.substringBefore("@")
                else -> "Bumi Lover"
            }

            val nameToShow = rawName?.split(" ")?.joinToString(" ") { word ->
                word.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                    else it.toString()
                }
            } ?: ""

            tvGreeting.text = getString(R.string.hello_placeholder, nameToShow)

            ivProfile.setOnClickListener { profileView ->
                val popupMenu = PopupMenu(requireContext(), profileView)

                val spannableName = SpannableString(nameToShow)
                spannableName.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    spannableName.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                popupMenu.menu.add(0, 1, 0, spannableName)
                popupMenu.menu.add(0, 2, 1, "Pengaturan Profil")
                popupMenu.menu.add(0, 3, 2, "Logout")

                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> {
                            Toast.makeText(requireContext(), "Logged in as $nameToShow", Toast.LENGTH_SHORT).show()
                            true
                        }
                        2 -> {
                            val intent = Intent(requireContext(), ProfilActivity::class.java)
                            startActivity(intent)
                            true
                        }
                        3 -> {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Konfirmasi Logout")
                                .setMessage("Apakah Anda yakin ingin keluar?")
                                .setPositiveButton("Ya") { _, _ ->
                                    auth.signOut()
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        Toast.makeText(requireContext(), "Berhasil Logout", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(requireContext(), LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        activity?.finish()
                                    }
                                }
                                .setNegativeButton("Tidak", null)
                                .show()
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }

        btnEdukasi.setOnClickListener {
            val intent = Intent(requireContext(), EdukasiActivity::class.java)
            startActivity(intent)
        }

        // ✅ TAMBAHAN NAVIGASI KE MISI (TANPA MERUBAH YANG LAIN)
        btnMisi.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MisiFragment())
                .addToBackStack(null)
                .commit()
        }

        updateUserName(currentUser)

        currentUser.reload().addOnCompleteListener {
            if (it.isSuccessful) {
                updateUserName(auth.currentUser)
            }
        }
    }
}