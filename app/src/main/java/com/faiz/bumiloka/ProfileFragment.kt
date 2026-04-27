package com.faiz.bumiloka

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase // PASTIKAN IMPORT INI ADA
import java.util.Locale

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val tvProfileName = view.findViewById<TextView>(R.id.tvProfileName)
        val btnPengaturan = view.findViewById<LinearLayout>(R.id.btnPengaturan)

        // Asumsi ID dari layout XML-mu (SESUAIKAN DENGAN ID DI fragment_profile.xml KAMU)
        val tvGelarUser = view.findViewById<TextView>(R.id.tvGelarUser) // Untuk teks "📍 Eco Warrior"
        val tvTotalPoinBanner = view.findViewById<TextView>(R.id.tvTotalPoinBanner) // Untuk teks poin besar di kartu hijau
        val tvTotalPoinGrid = view.findViewById<TextView>(R.id.tvTotalPoinGrid) // Untuk teks poin kecil di bawah piala

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

            // --- PANGGIL FUNGSI LOAD DATA FIREBASE DI SINI ---
            loadDataProfil(user.uid, tvGelarUser, tvTotalPoinBanner, tvTotalPoinGrid)
        }

        btnPengaturan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PengaturanFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    // Fungsi untuk mengambil data XP dan Level dari Firebase
    private fun loadDataProfil(
        userId: String,
        tvGelarUser: TextView?,
        tvTotalPoinBanner: TextView?,
        tvTotalPoinGrid: TextView?
    ) {
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        db.get().addOnSuccessListener { snapshot ->
            if (!isAdded) return@addOnSuccessListener // Keamanan fragment agar tidak crash

            // Ambil data XP dan Level (Default: 0 XP, Level 1)
            val currentXp = snapshot.child("xp").getValue(Int::class.java) ?: 0
            val currentLevel = snapshot.child("level").getValue(Int::class.java) ?: 1

            // Atur Gelar berdasarkan level
            val levelTitle = getLevelTitle(currentLevel)

            // Update teks di layar
            tvGelarUser?.text = "📍 $levelTitle"
            tvTotalPoinBanner?.text = currentXp.toString()
            tvTotalPoinGrid?.text = currentXp.toString()

        }.addOnFailureListener {
            Log.e("BUMILOKA_DEBUG", "Gagal load profil: ${it.message}")
        }
    }

    // Fungsi untuk memberikan nama gelar berdasarkan level (Sama seperti di HomeFragment)
    private fun getLevelTitle(level: Int): String {
        return when (level) {
            1 -> "Pemula"
            2 -> "Pengamat Bumi"
            3 -> "Pejuang Lingkungan"
            4 -> "Pahlawan Hijau"
            else -> "Eco Warrior"
        }
    }
}