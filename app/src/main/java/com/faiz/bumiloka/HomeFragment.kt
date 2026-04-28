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
import android.util.Log

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Cek Session: Jika tidak ada user yang login, arahkan ke LoginActivity
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
        val btnMisi = view.findViewById<CardView>(R.id.btnMisi)
        val btnTantangan = view.findViewById<CardView>(R.id.btnTantangan)
        val btnKuis = view.findViewById<CardView>(R.id.btnKuis)

        // Fungsi untuk memperbarui tampilan nama
        fun updateUserName(user: FirebaseUser?) {
            val rawName = when {
                !user?.displayName.isNullOrBlank() -> user?.displayName
                !user?.email.isNullOrBlank() -> user?.email?.substringBefore("@")
                else -> "Bumi Lover"
            }

            // Capitalize: Mengubah huruf pertama setiap kata menjadi huruf besar
            val nameToShow = rawName?.split(" ")?.joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            } ?: ""

            tvGreeting.text = getString(R.string.hello_placeholder, nameToShow)

            // Setup listener profil dengan nama terbaru
            ivProfile.setOnClickListener { profileView ->
                val popupMenu = PopupMenu(requireContext(), profileView)

                // Membuat username menjadi BOLD menggunakan SpannableString
                val spannableName = SpannableString(nameToShow)
                spannableName.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    spannableName.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Menyamakan ID Menu dengan ID di Listener
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
                            val intent = Intent(requireContext(), ProfileFragment::class.java)
                            startActivity(intent)
                            true
                        }
                        3 -> {
                            // Konfirmasi Logout
                            AlertDialog.Builder(requireContext())
                                .setTitle("Konfirmasi Logout")
                                .setMessage("Apakah Anda yakin ingin keluar?")
                                .setPositiveButton("Ya") { _, _ ->
                                    // Proses Logout
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
            requireProfile {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, EdukasiFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        btnMisi.setOnClickListener {
            requireProfile {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MisiFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        btnTantangan.setOnClickListener {
            requireProfile {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TantanganFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        btnKuis.setOnClickListener {
            requireProfile {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, QuizUtamaFragment())
                    .commit()
            }
        }

        // Tampilkan nama awal dari cache session
        updateUserName(currentUser)

        // Reload data dari server Firebase untuk memastikan Nama muncul (sinkronisasi)
        currentUser.reload().addOnCompleteListener {
            if (it.isSuccessful) {
                updateUserName(auth.currentUser)
            }
        }
        checkProfileOnce()
    }
    private fun checkProfileOnce() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference

        // Pastikan path-nya benar: users -> userId
        db.child("users").child(userId).get().addOnSuccessListener { snapshot ->
            // Ambil status profil secara akurat
            val isComplete = snapshot.child("isProfileComplete").getValue(Boolean::class.java) ?: false

            Log.d("BUMILOKA_DEBUG", "Cek profil UID: $userId | Status: $isComplete")

            if (!isComplete) {
                showLengkapiProfilDialog()
            }
        }.addOnFailureListener {
            Log.e("BUMILOKA_DEBUG", "Gagal koneksi Firebase: ${it.message}")
        }
    }

    private fun requireProfile(action: () -> Unit) {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference

        db.child("users").child(userId).get().addOnSuccessListener { snapshot ->
            val isComplete = snapshot.child("isProfileComplete").getValue(Boolean::class.java) ?: false

            if (isComplete) {
                action()
            } else {
                showLengkapiProfilDialog()
            }
        }
    }

    // Fungsi pembantu agar tidak menulis ulang dialog berkali-kali
    private fun showLengkapiProfilDialog() {
        if (!isAdded) return
        AlertDialog.Builder(requireContext())
            .setTitle("Lengkapi Profil")
            .setMessage("Silakan lengkapi profil terlebih dahulu agar dapat mengakses semua fitur.")
            .setCancelable(false)
            .setPositiveButton("Lengkapi") { _, _ ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PengaturanFragment())
                    .addToBackStack(null)
                    .commit()
            }
            .show()
    }
}
