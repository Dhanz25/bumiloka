package com.faiz.bumiloka

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
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

    // Deklarasi view untuk progress agar bisa diakses dari fungsi mana saja
    private lateinit var tvLevelTitle: TextView
    private lateinit var pbTargetProgress: ProgressBar
    private lateinit var tvProgressPercentage: TextView

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
        val btnMisi = view.findViewById<CardView>(R.id.btnMisi)
        val btnTantangan = view.findViewById<CardView>(R.id.btnTantangan)
        val btnKuis = view.findViewById<CardView>(R.id.btnKuis)

        // Inisialisasi View Progress
        tvLevelTitle = view.findViewById(R.id.tvLevelTitle)
        pbTargetProgress = view.findViewById(R.id.pbTargetProgress)
        tvProgressPercentage = view.findViewById(R.id.tvProgressPercentage)

        // Tampilkan nilai awal (Level 1, 0%) saat memuat data
        updateUserProgress(1, "Pemula", 0)

        // Fungsi untuk memperbarui tampilan nama
        fun updateUserName(user: FirebaseUser?) {
            val rawName = when {
                !user?.displayName.isNullOrBlank() -> user?.displayName
                !user?.email.isNullOrBlank() -> user?.email?.substringBefore("@")
                else -> "Bumi Lover"
            }

            val nameToShow = rawName?.split(" ")?.joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
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
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, PengaturanFragment())
                                .addToBackStack(null)
                                .commit()
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
                    .addToBackStack(null)
                    .commit()
            }
        }

        updateUserName(currentUser)

        currentUser.reload().addOnCompleteListener {
            if (it.isSuccessful) {
                updateUserName(auth.currentUser)
            }
        }

        // Panggil fungsi yang sudah disatukan untuk mengecek profil dan memuat progress
        loadUserData()
    }

    // Fungsi terpisah agar kode lebih rapi
    private fun updateUserProgress(level: Int, title: String, progress: Int) {
        if (!isAdded) return // Keamanan mencegah crash jika fragment sudah tertutup

        tvLevelTitle.text = "🏅 Level $level - $title"
        pbTargetProgress.progress = progress

        val motivasi = when {
            progress == 0 -> "Ayo mulai!"
            progress < 50 -> "Awal yang bagus!"
            progress < 100 -> "Terus semangat!"
            else -> "Target tercapai! Hebat!"
        }
        tvProgressPercentage.text = "$progress% tercapai - $motivasi"
    }

    // Fungsi tunggal untuk mengambil semua data dari node "users/{userId}"
    private fun loadUserData() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference

        db.child("users").child(userId).get().addOnSuccessListener { snapshot ->
            // 1. Cek Status Profil
            val isComplete = snapshot.child("isProfileComplete").getValue(Boolean::class.java) ?: false
            if (!isComplete) {
                showLengkapiProfilDialog()
            }

            // 2. Ambil Data Level & XP (Default: Level 1, 0 XP)
            val currentLevel = snapshot.child("level").getValue(Int::class.java) ?: 1
            val currentXp = snapshot.child("xp").getValue(Int::class.java) ?: 0

            // 3. Sistem Perhitungan Progress
            // Target XP bertambah setiap level (Contoh: Lvl 1 butuh 100 XP, Lvl 2 butuh 200 XP)
            val targetXp = currentLevel * 100

            // Hitung persentase untuk Progress Bar
            val progressPercent = if (targetXp > 0) ((currentXp.toDouble() / targetXp) * 100).toInt() else 0

            // Ambil gelar level secara dinamis
            val levelTitle = getLevelTitle(currentLevel)

            // 4. Update UI
            updateUserProgress(currentLevel, levelTitle, progressPercent)

        }.addOnFailureListener {
            Log.e("BUMILOKA_DEBUG", "Gagal koneksi Firebase: ${it.message}")
        }
    }

    // Fungsi tambahan untuk memberikan nama gelar berdasarkan level
    private fun getLevelTitle(level: Int): String {
        return when (level) {
            1 -> "Pemula"
            2 -> "Pengamat Bumi"
            3 -> "Pejuang Lingkungan"
            4 -> "Pahlawan Hijau"
            else -> "Eco Warrior"
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