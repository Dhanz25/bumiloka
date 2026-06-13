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
import com.google.firebase.database.FirebaseDatabase
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

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val tvProfileName = view.findViewById<TextView>(R.id.tvProfileName)
        val btnPengaturan = view.findViewById<LinearLayout>(R.id.btnPengaturan)
        val btnBantuan = view.findViewById<LinearLayout>(R.id.btnBantuan)
        val btnResetData = view.findViewById<LinearLayout>(R.id.btnResetData)
        val btnLogout = view.findViewById<LinearLayout>(R.id.btnLogout)

        val tvGelarUser = view.findViewById<TextView>(R.id.tvGelarUser)
        val tvTotalPoinBanner = view.findViewById<TextView>(R.id.tvTotalPoinBanner)
        val tvTotalPoinGrid = view.findViewById<TextView>(R.id.tvTotalPoinGrid)
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
            loadDataProfil(user.uid, tvGelarUser, tvTotalPoinBanner, tvTotalPoinGrid, tvTotalMisi, tvTotalLencana)
        }

        btnPengaturan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PengaturanFragment())
                .addToBackStack(null)
                .commit()
        }

        btnBantuan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, bantuan_dukungan())
                .addToBackStack(null)
                .commit()
        }

        // --- LOGIKA RESET DATA ---
        btnResetData.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Reset Data")
                .setMessage("Semua progres, badge, level, quiz, materi, tantangan, dan poin akan dihapus. Lanjutkan?")
                .setPositiveButton("Reset") { _, _ ->
                    AppResetHelper.resetSemuaData(requireContext()) { success ->
                        if (success) {
                            Toast.makeText(requireContext(), "Data berhasil direset", Toast.LENGTH_SHORT).show()
                            currentUser?.let { user ->
                                loadDataProfil(user.uid, tvGelarUser, tvTotalPoinBanner, tvTotalPoinGrid, tvTotalMisi, tvTotalLencana)
                            }
                        } else {
                            Toast.makeText(requireContext(), "Gagal mereset data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Keluar")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ ->
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
        }
    }

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
            if (!isAdded) return@addOnSuccessListener

            val totalPoint = snapshot.child("totalPoint").getValue(Int::class.java) ?: 0
            val currentLevel = snapshot.child("level").getValue(Int::class.java) ?: 1
            val misiTercapai = snapshot.child("misiTercapai").getValue(Int::class.java) ?: 0
            val totalLencana = BadgeHelper.getTotalBadge(requireContext())

            val levelTitle = getLevelTitle(currentLevel)

            tvGelarUser?.text = "📍 $levelTitle"
            tvTotalPoinBanner?.text = totalPoint.toString()
            tvTotalPoinGrid?.text = totalPoint.toString()
            tvTotalMisi?.text = misiTercapai.toString()
            tvTotalLencana?.text = totalLencana.toString()

        }.addOnFailureListener {
            Log.e("BUMILOKA_DEBUG", "Gagal load profil: ${it.message}")
        }
    }

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
