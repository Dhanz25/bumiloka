package com.faiz.bumiloka

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.faiz.bumiloka.ui.login.LoginActivity
import com.faiz.bumiloka.ui.notification.NotificationViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val notificationViewModel: NotificationViewModel by viewModels()

    private lateinit var rlProfileBackground: RelativeLayout
    private lateinit var ivOrnamenProfile1: ImageView
    private lateinit var ivOrnamenProfile2: ImageView
    private lateinit var ivOrnamenProfile3: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
        loadDataProfil(auth.currentUser?.uid ?: "")
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

        // Inisialisasi View
        val tvProfileName = view.findViewById<TextView>(R.id.tvProfileName)
        val btnPengaturan = view.findViewById<LinearLayout>(R.id.btnPengaturan)
        val btnBantuan = view.findViewById<LinearLayout>(R.id.btnBantuan)
        val btnNotifikasi = view.findViewById<LinearLayout>(R.id.btnNotifikasi)
        val btnLogout = view.findViewById<LinearLayout>(R.id.btnLogout)
        val tvNotificationBadge = view.findViewById<TextView>(R.id.tvNotificationBadge)

        rlProfileBackground = view.findViewById(R.id.rlProfileBackground)
        ivOrnamenProfile1 = view.findViewById(R.id.ivOrnamenProfile1)
        ivOrnamenProfile2 = view.findViewById(R.id.ivOrnamenProfile2)
        ivOrnamenProfile3 = view.findViewById(R.id.ivOrnamenProfile3)

        val localLevel = LevelHelper.getCurrentLevelLocal(requireContext())
        updateProfileTheme(localLevel)

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
            loadDataProfil(user.uid)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationViewModel.unreadCount.collectLatest { count ->
                    if (count > 0) {
                        tvNotificationBadge.visibility = View.VISIBLE
                        tvNotificationBadge.text = count.toString()
                    } else {
                        tvNotificationBadge.visibility = View.GONE
                    }
                }
            }
        }

        btnPengaturan.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, PengaturanFragment()).addToBackStack(null).commit()
        }

        btnBantuan.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, bantuan_dukungan()).addToBackStack(null).commit()
        }
        
        btnNotifikasi.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, NotificationFragment()).addToBackStack(null).commit()
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Keluar")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ ->
                    auth.signOut()
                    googleSignInClient.signOut().addOnCompleteListener {
                        Toast.makeText(requireContext(), "Berhasil Keluar", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        activity?.finish()
                    }
                }
                .setNegativeButton("Tidak", null)
                .show()
        }
    }

    private fun loadDataProfil(userId: String) {
        if (userId.isEmpty()) return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        db.get().addOnSuccessListener { snapshot ->
            if (!isAdded) return@addOnSuccessListener

            val totalPoint = snapshot.child("totalPoint").getValue(Int::class.java) ?: 0
            val currentLevel = snapshot.child("level").getValue(Int::class.java) ?: 1
            val misiTercapai = snapshot.child("misiTercapai").getValue(Int::class.java) ?: 0
            val totalLencana = BadgeHelper.getTotalBadge(requireContext())

            val tvGelarUser = view?.findViewById<TextView>(R.id.tvGelarUser)
            val tvTotalPoinBanner = view?.findViewById<TextView>(R.id.tvTotalPoinBanner)
            val tvTotalPoinGrid = view?.findViewById<TextView>(R.id.tvTotalPoinGrid)
            val tvTotalMisi = view?.findViewById<TextView>(R.id.tvTotalMisi)
            val tvTotalLencana = view?.findViewById<TextView>(R.id.tvTotalLencana)

            tvGelarUser?.text = "📍 ${getLevelTitle(currentLevel)}"
            tvTotalPoinBanner?.text = totalPoint.toString()
            tvTotalPoinGrid?.text = totalPoint.toString()
            tvTotalMisi?.text = misiTercapai.toString()
            tvTotalLencana?.text = totalLencana.toString()

            updateProfileTheme(currentLevel)

        }.addOnFailureListener {
            Log.e("BUMILOKA_DEBUG", "Gagal load profil: ${it.message}")
        }
    }

    private fun updateProfileTheme(level: Int) {
        if (!isAdded) return
        when (level) {
            1 -> {
                rlProfileBackground.setBackgroundResource(R.drawable.bg_level1)
                ivOrnamenProfile1.setImageResource(R.drawable.ic_leaf)
                ivOrnamenProfile2.setImageResource(R.drawable.ic_eco)
                ivOrnamenProfile3.setImageResource(R.drawable.ic_leaf)
            }
            2 -> {
                rlProfileBackground.setBackgroundResource(R.drawable.bg_level2)
                ivOrnamenProfile1.setImageResource(R.drawable.ic_water)
                ivOrnamenProfile2.setImageResource(R.drawable.ic_bolt)
                ivOrnamenProfile3.setImageResource(R.drawable.ic_water)
            }
            3 -> {
                rlProfileBackground.setBackgroundResource(R.drawable.bg_level3)
                ivOrnamenProfile1.setImageResource(R.drawable.ic_trophy)
                ivOrnamenProfile2.setImageResource(R.drawable.ic_medal)
                ivOrnamenProfile3.setImageResource(R.drawable.ic_trophy)
            }
            else -> {
                rlProfileBackground.setBackgroundResource(R.drawable.bg_level1)
                ivOrnamenProfile1.setImageResource(R.drawable.ic_leaf)
                ivOrnamenProfile2.setImageResource(R.drawable.ic_eco)
                ivOrnamenProfile3.setImageResource(R.drawable.ic_leaf)
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }
}
