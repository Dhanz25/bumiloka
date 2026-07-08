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
import com.faiz.bumiloka.model.Badge
import com.faiz.bumiloka.ui.login.LoginActivity
import com.faiz.bumiloka.ui.notification.NotificationViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val notificationViewModel: NotificationViewModel by viewModels()

    // Class properties to avoid unresolved references
    private lateinit var rlProfileBackground: RelativeLayout
    private lateinit var ivOrnamenProfile1: ImageView
    private lateinit var ivOrnamenProfile2: ImageView
    private lateinit var ivOrnamenProfile3: ImageView
    private lateinit var containerEarnedBadges: LinearLayout
    private lateinit var tvEmptyBadges: TextView
    private lateinit var scrollBadges: View
    
    private lateinit var tvProfileName: TextView
    private lateinit var tvGelarUser: TextView
    private lateinit var tvTotalPoinBanner: TextView
    private lateinit var tvTotalPoinGrid: TextView
    private lateinit var tvTotalMisi: TextView
    private lateinit var tvTotalLencana: TextView
    
    private lateinit var btnPengaturan: LinearLayout
    private lateinit var btnBantuan: LinearLayout
    private lateinit var btnNotifikasi: LinearLayout
    private lateinit var btnLogout: LinearLayout
    private lateinit var tvNotificationBadge: TextView

    private var userRef: DatabaseReference? = null
    private var userListener: ValueEventListener? = null

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

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // Bind all views carefully
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvGelarUser = view.findViewById(R.id.tvGelarUser)
        tvTotalPoinBanner = view.findViewById(R.id.tvTotalPoinBanner)
        tvTotalPoinGrid = view.findViewById(R.id.tvTotalPoinGrid)
        tvTotalMisi = view.findViewById(R.id.tvTotalMisi)
        tvTotalLencana = view.findViewById(R.id.tvTotalLencana)
        
        btnPengaturan = view.findViewById(R.id.btnPengaturan)
        btnBantuan = view.findViewById(R.id.btnBantuan)
        btnNotifikasi = view.findViewById(R.id.btnNotifikasi)
        btnLogout = view.findViewById(R.id.btnLogout)
        tvNotificationBadge = view.findViewById(R.id.tvNotificationBadge)
        
        containerEarnedBadges = view.findViewById(R.id.containerEarnedBadges)
        tvEmptyBadges = view.findViewById(R.id.tvEmptyBadges)
        scrollBadges = view.findViewById(R.id.scrollBadges)
        rlProfileBackground = view.findViewById(R.id.rlProfileBackground)
        ivOrnamenProfile1 = view.findViewById(R.id.ivOrnamenProfile1)
        ivOrnamenProfile2 = view.findViewById(R.id.ivOrnamenProfile2)
        ivOrnamenProfile3 = view.findViewById(R.id.ivOrnamenProfile3)

        // Navigation
        view.findViewById<View>(R.id.btnStatLencana).setOnClickListener { 
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, LencanaFragment()).addToBackStack(null).commit() 
        }
        view.findViewById<View>(R.id.btnStatMisi).setOnClickListener { 
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, MisiFragment()).addToBackStack(null).commit() 
        }
        
        btnPengaturan.setOnClickListener { parentFragmentManager.beginTransaction().replace(R.id.fragment_container, PengaturanFragment()).addToBackStack(null).commit() }
        btnBantuan.setOnClickListener { parentFragmentManager.beginTransaction().replace(R.id.fragment_container, bantuan_dukungan()).addToBackStack(null).commit() }
        btnNotifikasi.setOnClickListener { parentFragmentManager.beginTransaction().replace(R.id.fragment_container, NotificationFragment()).addToBackStack(null).commit() }

        currentUser?.let { user ->
            val rawName = user.displayName ?: user.email?.substringBefore("@") ?: "Bumi Lover"
            tvProfileName.text = rawName.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
            startRealtimeUpdates(user.uid)
        }

        // Notification Observer
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationViewModel.unreadCount.collectLatest { count ->
                    tvNotificationBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
                    tvNotificationBadge.text = count.toString()
                }
            }
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext()).setTitle("Keluar").setMessage("Yakin ingin keluar?").setPositiveButton("Ya") { _, _ ->
                auth.signOut()
                googleSignInClient.signOut().addOnCompleteListener {
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
                    activity?.finish()
                }
            }.setNegativeButton("Tidak", null).show()
        }
    }

    private fun startRealtimeUpdates(userId: String) {
        userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                
                val currentLevel = (snapshot.child("level").value as? Long)?.toInt() ?: 1
                val totalPoint = (snapshot.child("totalPoint").value as? Long)?.toInt() ?: 0
                
                // 1. Calculate Mission Count (9 Primary + Bonus + History)
                var totalMisi = 0
                
                // From progress_sync
                val syncNode = snapshot.child("progress_sync")
                for (lvl in 1..3) {
                    for (m in 1..3) {
                        val key = "MISI_${lvl}_misi${m}_selesai"
                        val valObj = syncNode.child(key).value
                        if (valObj == true || valObj == 1L || valObj == 1) totalMisi++
                    }
                }
                
                // From challengeProgress
                snapshot.child("challengeProgress").children.forEach { child ->
                    val completed = child.child("completed").value
                    if (completed == true || completed == 1L || completed == 1) totalMisi++
                }
                
                // From historical counter
                val historyCount = (snapshot.child("misiTercapai").value as? Long)?.toInt() ?: 0
                if (totalMisi < historyCount) totalMisi = historyCount

                // 2. Collect Unique Badge IDs
                val ownedIds = mutableSetOf<String>()
                snapshot.child("badges_earned").children.forEach { it.key?.let { key -> ownedIds.add(key) } }
                snapshot.child("lencana").children.forEach { it.key?.let { key -> ownedIds.add(key) } }

                // 3. Update UI Stats
                tvGelarUser.text = "📍 ${getLevelTitle(currentLevel)}"
                tvTotalPoinBanner.text = totalPoint.toString()
                tvTotalPoinGrid.text = totalPoint.toString()
                tvTotalMisi.text = totalMisi.toString()
                tvTotalLencana.text = ownedIds.size.toString()

                updateProfileTheme(currentLevel)
                BadgeHelper.syncBadges(requireContext())
                renderEarnedBadgesHorizontal(ownedIds.toList())
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        userRef?.addValueEventListener(userListener!!)
    }

    private fun renderEarnedBadgesHorizontal(ownedIds: List<String>) {
        if (!isAdded) return
        containerEarnedBadges.removeAllViews()
        
        if (ownedIds.isEmpty()) {
            scrollBadges.visibility = View.GONE
            tvEmptyBadges.visibility = View.VISIBLE
            return
        }

        scrollBadges.visibility = View.VISIBLE
        tvEmptyBadges.visibility = View.GONE

        // Step 1: Render placeholders immediately
        ownedIds.forEach { id ->
            addBadgeToContainer(Badge(id = id, nama = "Lencana", level = 1))
        }

        // Step 2: Update with real metadata from /badges
        FirebaseDatabase.getInstance().reference.child("badges")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val badgeMap = mutableMapOf<String, Badge>()
                    snapshot.children.forEach { child ->
                        val b = child.getValue(Badge::class.java) ?: return@forEach
                        b.id = child.key ?: ""
                        badgeMap[b.id] = b
                    }

                    containerEarnedBadges.removeAllViews()
                    ownedIds.forEach { id ->
                        val finalBadge = badgeMap[id] ?: Badge(id = id, nama = "Lencana Baru", level = 1)
                        addBadgeToContainer(finalBadge)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun addBadgeToContainer(badge: Badge) {
        val badgeView = layoutInflater.inflate(R.layout.item_badge_profile_compact, containerEarnedBadges, false)
        val ivBadge = badgeView.findViewById<ImageView>(R.id.ivBadgeIcon)
        val tvName = badgeView.findViewById<TextView>(R.id.tvBadgeName)

        tvName.text = badge.nama
        BadgeVisualHelper.renderBadge(ivBadge, badge.nama, badge.level)

        badgeView.setOnClickListener {
            val d = if (badge.deskripsi.isEmpty()) "Lencana spesial BumiLoka" else badge.deskripsi
            Toast.makeText(requireContext(), "${badge.nama}: $d", Toast.LENGTH_SHORT).show()
        }
        containerEarnedBadges.addView(badgeView)
    }

    private fun updateProfileTheme(level: Int) {
        if (!isAdded) return
        val bgRes = when (level) {
            1 -> R.drawable.bg_level1
            2 -> R.drawable.bg_level2
            3 -> R.drawable.bg_level3
            else -> R.drawable.bg_level1
        }
        rlProfileBackground.setBackgroundResource(bgRes)
        
        when (level) {
            1 -> {
                ivOrnamenProfile1.setImageResource(R.drawable.ic_leaf)
                ivOrnamenProfile2.setImageResource(R.drawable.ic_eco)
                ivOrnamenProfile3.setImageResource(R.drawable.ic_leaf)
            }
            2 -> {
                ivOrnamenProfile1.setImageResource(R.drawable.ic_water)
                ivOrnamenProfile2.setImageResource(R.drawable.ic_bolt)
                ivOrnamenProfile3.setImageResource(R.drawable.ic_water)
            }
            3 -> {
                ivOrnamenProfile1.setImageResource(R.drawable.ic_trophy)
                ivOrnamenProfile2.setImageResource(R.drawable.ic_medal)
                ivOrnamenProfile3.setImageResource(R.drawable.ic_trophy)
            }
            else -> {
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
        userListener?.let { userRef?.removeEventListener(it) }
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }
}
