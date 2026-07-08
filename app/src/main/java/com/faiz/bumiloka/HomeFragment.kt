package com.faiz.bumiloka

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.model.BonusChallengeModel
import com.faiz.bumiloka.ui.login.LoginActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var mAdView: AdView? = null
    private lateinit var pbProgress: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvLevel: TextView
    private lateinit var rlLevelBackground: RelativeLayout
    private lateinit var ivOrnamen1: ImageView
    private lateinit var ivOrnamen2: ImageView
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        mAdView = view.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView?.adListener = object : AdListener() {
            override fun onAdLoaded() { Log.d("ADMOB", "Banner Loaded") }
            override fun onAdFailedToLoad(error: LoadAdError) { Log.e("ADMOB", "Error : ${error.message}") }
        }
        mAdView?.loadAd(adRequest)
        return view
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
        loadProgressFirebase()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE

        pbProgress = view.findViewById(R.id.pbTargetProgress)
        tvProgress = view.findViewById(R.id.tvProgressPercentage)
        tvLevel = view.findViewById(R.id.tvLevelTitle)
        rlLevelBackground = view.findViewById(R.id.rlLevelBackground)
        ivOrnamen1 = view.findViewById(R.id.ivOrnamen1)
        ivOrnamen2 = view.findViewById(R.id.ivOrnamen2)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            val context = context ?: return
            startActivity(Intent(context, LoginActivity::class.java))
            activity?.finish()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        view.findViewById<View>(R.id.btnEdukasi).setOnClickListener {
            requireProfile { parentFragmentManager.beginTransaction().replace(R.id.fragment_container, EdukasiFragment()).addToBackStack(null).commit() }
        }
        view.findViewById<View>(R.id.btnMisi).setOnClickListener {
            requireProfile { parentFragmentManager.beginTransaction().replace(R.id.fragment_container, MisiFragment()).addToBackStack(null).commit() }
        }
        view.findViewById<View>(R.id.btnTantangan).setOnClickListener {
            requireProfile { parentFragmentManager.beginTransaction().replace(R.id.fragment_container, TantanganFragment()).addToBackStack(null).commit() }
        }
        view.findViewById<View>(R.id.btnKuis).setOnClickListener {
            requireProfile { parentFragmentManager.beginTransaction().replace(R.id.fragment_container, QuizUtamaFragment()).addToBackStack(null).commit() }
        }
        view.findViewById<View>(R.id.btnLevelDashboard).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, LevelFragment()).addToBackStack(null).commit()
        }
        
        view.findViewById<Button>(R.id.btnMulaiKuisJawa).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, BahasaJawaFragment()).addToBackStack(null).commit()
        }

        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)
        val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)
        
        updateUserName(currentUser, tvGreeting, ivProfile)
        tampilkanRekomendasiHarian(view)

        val ctx = context ?: return
        val prefs = ctx.getSharedPreferences("APP", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("sudah_welcome", false)) {
            showWelcomeThenProfil()
            prefs.edit().putBoolean("sudah_welcome", true).apply()
        }
    }

    private fun updateUserName(user: FirebaseUser?, tvGreeting: TextView, ivProfile: ImageView) {
        val rawName = user?.displayName ?: user?.email?.substringBefore("@") ?: "Bumi Lover"
        val nameToShow = rawName.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.titlecase() } }
        tvGreeting.text = "Halo, $nameToShow! 👋"

        ivProfile.setOnClickListener { profileView ->
            if (!isAdded) return@setOnClickListener
            val inflater = LayoutInflater.from(requireContext())
            val menuView = inflater.inflate(R.layout.layout_profile_menu, null)
            val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

            menuView.findViewById<TextView>(R.id.menuUserName).text = nameToShow
            menuView.findViewById<View>(R.id.menuPengaturan).setOnClickListener {
                popupWindow.dismiss()
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, PengaturanFragment()).addToBackStack(null).commit()
            }

            menuView.findViewById<View>(R.id.menuKeluar).setOnClickListener {
                popupWindow.dismiss()
                AlertDialog.Builder(requireContext()).setTitle("Konfirmasi Keluar").setMessage("Apakah Anda yakin ingin keluar?")
                    .setPositiveButton("Ya") { _, _ ->
                        auth.signOut()
                        googleSignInClient.signOut().addOnCompleteListener { 
                            val context = context ?: return@addOnCompleteListener
                            startActivity(Intent(context, LoginActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
                            activity?.finish()
                        }
                    }.setNegativeButton("Tidak", null).show()
            }

            popupWindow.elevation = 0f
            popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            popupWindow.showAsDropDown(profileView, -320, 10, Gravity.END)
        }
    }

    private fun loadProgressFirebase() {
        val ctx = context ?: return
        val userId = auth.currentUser?.uid ?: return
        LevelHelper.getCurrentLevel(ctx) { level ->
            if (!isAdded) return@getCurrentLevel
            when(level) {
                1 -> {
                    rlLevelBackground.setBackgroundResource(R.drawable.bg_level1)
                    ivOrnamen1.setImageResource(R.drawable.ic_leaf)
                    ivOrnamen2.setImageResource(R.drawable.ic_eco)
                    pbProgress.progressTintList = ColorStateList.valueOf(Color.parseColor("#00E676"))
                }
                2 -> {
                    rlLevelBackground.setBackgroundResource(R.drawable.bg_level2)
                    ivOrnamen1.setImageResource(R.drawable.ic_water)
                    ivOrnamen2.setImageResource(R.drawable.ic_bolt)
                    pbProgress.progressTintList = ColorStateList.valueOf(Color.parseColor("#448AFF"))
                }
                3 -> {
                    rlLevelBackground.setBackgroundResource(R.drawable.bg_level3)
                    ivOrnamen1.setImageResource(R.drawable.ic_trophy)
                    ivOrnamen2.setImageResource(R.drawable.ic_medal)
                    pbProgress.progressTintList = ColorStateList.valueOf(Color.parseColor("#FFD600"))
                }
            }

            val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)
            db.get().addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener
                val highest = (snapshot.child("highestUnlockedLevel").value as? Long)?.toInt() ?: 1
                val xp = (snapshot.child("xp").value as? Long)?.toInt() ?: 0
                val targetXP = level * 100
                var progressPercent = ((xp.toDouble() / targetXP) * 100).toInt()
                if (level < highest) progressPercent = 100
                
                pbProgress.progress = progressPercent
                tvProgress.text = if (level < highest) "Selesai ✓" else "$progressPercent% tercapai"
                tvLevel.text = "🏅 Level $level ${if (level < highest) "(Riwayat)" else ""}"
            }
        }
    }

    private fun tampilkanRekomendasiHarian(view: View) {
        val ctx = context ?: return
        val card = view.findViewById<View>(R.id.cardRekomendasi)
        val tvRekomendasi = view.findViewById<TextView>(R.id.tvRekomendasiHariIni)
        val sharedPref = ctx.getSharedPreferences("RekomendasiHarian", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        val dbBonus = FirebaseDatabase.getInstance().reference.child("bonus_tantangan")
        dbBonus.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                val listBonus = mutableListOf<BonusChallengeModel>()
                for (child in snapshot.children) {
                    try {
                        val map = child.value as? Map<*, *> ?: continue
                        val b = BonusChallengeModel(
                            id = child.key ?: "",
                            judul = map["judul"]?.toString() ?: "",
                            deskripsi = map["deskripsi"]?.toString() ?: "",
                            type = map["type"]?.toString() ?: "COMMITMENT",
                            targetDays = (map["targetDays"] as? Long)?.toInt() ?: 1,
                            quizId = map["quizId"]?.toString() ?: "",
                            badgeId = map["badgeId"]?.toString() ?: "",
                            aktif = map["aktif"] as? Boolean ?: true
                        )
                        if (b.aktif) listBonus.add(b)
                    } catch (e: Exception) { Log.e("Home", "Error parse bonus: ${e.message}") }
                }

                if (listBonus.isNotEmpty()) {
                    val savedId = sharedPref.getString("bonus_id", "")
                    val savedDate = sharedPref.getString("tanggal", "")
                    
                    val terpilih = if (savedDate == today && savedId != null) {
                        listBonus.find { it.id == savedId } ?: listBonus.random()
                    } else {
                        listBonus.random()
                    }
                    
                    sharedPref.edit()
                        .putString("tanggal", today)
                        .putString("bonus_id", terpilih.id)
                        .putString("rekomendasi", terpilih.judul)
                        .apply()

                    tvRekomendasi.text = terpilih.judul
                    card.setOnClickListener {
                        if (!isAdded) return@setOnClickListener
                        val fragment = DetailTantanganBonusFragment()
                        val bundle = Bundle().apply {
                            putString("id", terpilih.id)
                            putString("judul", terpilih.judul)
                            putString("deskripsi", terpilih.deskripsi)
                            putString("type", terpilih.type)
                            putInt("targetDays", terpilih.targetDays)
                            putString("quizId", terpilih.quizId)
                            putString("badgeId", terpilih.badgeId)
                        }
                        fragment.arguments = bundle
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                } else {
                    tvRekomendasi.text = "Ayo jaga bumi hari ini!"
                }
            }
            override fun onCancelled(error: DatabaseError) {
                if (isAdded) tvRekomendasi.text = "🌱 Mari beraksi untuk bumi!"
            }
        })
    }

    private fun requireProfile(action: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference.child("users").child(userId).get().addOnSuccessListener { snapshot ->
            if (!isAdded) return@addOnSuccessListener
            if (snapshot.child("isProfileComplete").getValue(Boolean::class.java) == true) action() else showLengkapiProfilDialog()
        }
    }

    private fun showWelcomeThenProfil() {
        if (!isAdded) return
        val inflater = LayoutInflater.from(requireContext())
        val toastView = inflater.inflate(R.layout.toast_welcome, null)
        toastView.findViewById<TextView>(R.id.tvToastMessage).text = "Selamat Datang! 👋"
        Toast(requireContext()).apply { duration = Toast.LENGTH_SHORT; view = toastView; setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 200); show() }
        Handler(Looper.getMainLooper()).postDelayed({ if(isAdded) requireProfile {} }, 1500)
    }

    private fun showLengkapiProfilDialog() {
        if (!isAdded) return
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.pop_up_profile, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        view.setOnClickListener { 
            dialog.dismiss()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, PengaturanFragment()).addToBackStack(null).commit() 
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAdView?.destroy()
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }
}
