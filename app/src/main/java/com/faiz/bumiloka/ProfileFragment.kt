package com.faiz.bumiloka

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.ui.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase // PASTIKAN IMPORT INI ADA
import java.util.Locale

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

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
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val tvProfileName = view.findViewById<TextView>(R.id.tvProfileName)
        val btnPengaturan = view.findViewById<LinearLayout>(R.id.btnPengaturan)
        val btnLogout = view.findViewById<LinearLayout>(R.id.btnLogout)


        // Asumsi ID dari layout XML-mu (SESUAIKAN DENGAN ID DI fragment_profile.xml KAMU)
        val tvGelarUser = view.findViewById<TextView>(R.id.tvGelarUser) // Untuk teks "📍 Eco Warrior"
        val tvTotalPoinBanner = view.findViewById<TextView>(R.id.tvTotalPoinBanner) // Untuk teks poin besar di kartu hijau
        val tvTotalPoinGrid = view.findViewById<TextView>(R.id.tvTotalPoinGrid) // Untuk teks poin kecil di bawah piala
        val tvTotalMisi = view.findViewById<TextView>(R.id.tvTotalMisi)
        val tvTotalLencana = view.findViewById<TextView>(R.id.tvTotalLencana)

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
            loadDataProfil(user.uid, tvGelarUser, tvTotalPoinBanner, tvTotalPoinGrid, tvTotalMisi, tvTotalLencana)
        }

        btnPengaturan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PengaturanFragment())
                .addToBackStack(null)
                .commit()
        }
        btnLogout.setOnClickListener {

            // Konfirmasi Logout
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Keluar")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ ->
                    // Proses Logout
                    auth.signOut()
                    googleSignInClient.signOut().addOnCompleteListener {
                        Toast.makeText(requireContext(), "Berhasil Keluar", Toast.LENGTH_SHORT).show()
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
    }

    // Fungsi untuk mengambil data XP dan Level dari Firebase
    private fun loadDataProfil(
        userId: String,
        tvGelarUser: TextView?,
        tvTotalPoinBanner: TextView?,
        tvTotalPoinGrid: TextView?,
        tvTotalMisi: TextView?,
        tvTotalLencana: TextView?
    ) {
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        db.get().addOnSuccessListener { snapshot ->
            if (!isAdded) return@addOnSuccessListener // Keamanan fragment agar tidak crash

            // Ambil data XP dan Level (Default: 0 XP, Level 1)
            val totalPoint = snapshot.child("totalPoint")
                .getValue(Int::class.java) ?: 0
            val currentLevel = snapshot.child("level").getValue(Int::class.java) ?: 1

            // --- AMBIL DATA MISI & LENCANA (Default 0) ---
            val misiTercapai = snapshot.child("misiTercapai").getValue(Int::class.java) ?: 0
            val totalLencana = snapshot.child("totalLencana").getValue(Int::class.java) ?: 0

            // Atur Gelar berdasarkan level
            val levelTitle = getLevelTitle(currentLevel)

            // Update teks di layar
            tvGelarUser?.text = "📍 $levelTitle"
            tvTotalPoinBanner?.text = totalPoint.toString()
            tvTotalPoinGrid?.text = totalPoint.toString()
            tvTotalMisi?.text = misiTercapai.toString()
            tvTotalLencana?.text = totalLencana.toString()

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