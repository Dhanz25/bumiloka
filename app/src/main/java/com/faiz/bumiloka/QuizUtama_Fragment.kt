package com.faiz.bumiloka

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.faiz.bumiloka.model.Kuis
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class QuizUtamaFragment : Fragment(R.layout.fragment_quiz_utama_) {

    private lateinit var containerKuis: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvLevelIndicator: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var tabAll: TextView
    private lateinit var tabSelesai: TextView
    private lateinit var tabBelum: TextView
    
    private var userLevel = 1
    private val db = FirebaseDatabase.getInstance().getReference("kuis")
    private var currentFilter = "ALL"
    private var forcedLevel = -1

    companion object {
        fun newInstance(level: Int = -1): QuizUtamaFragment {
            return QuizUtamaFragment().apply {
                arguments = Bundle().apply { putInt("FORCED_LEVEL", level) }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

        forcedLevel = arguments?.getInt("FORCED_LEVEL", -1) ?: -1

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        tvLevelIndicator = view.findViewById(R.id.tvLevelIndicator)
        containerKuis = view.findViewById(R.id.containerKuis)
        progressBar = view.findViewById(R.id.progressBarKuis)
        tvEmpty = view.findViewById(R.id.tvEmptyKuis)
        
        tabAll = view.findViewById(R.id.tab_all)
        tabSelesai = view.findViewById(R.id.tab_selesai)
        tabBelum = view.findViewById(R.id.tab_belum)

        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        val ctx = context ?: return
        LevelHelper.getCurrentLevel(ctx) { level ->
            if (!isAdded) return@getCurrentLevel
            userLevel = if (forcedLevel != -1) forcedLevel else level

            val levelName = when (userLevel) {
                1 -> "Benih Kesadaran"; 2 -> "Tunas Kepedulian"; 3 -> "Pohon Kelestarian"; else -> "Hero"
            }
            tvLevelIndicator.text = "Level $userLevel ($levelName)"
            loadKuisFromFirebase(currentFilter)
        }

        tabAll.setOnClickListener { 
            updateTabUI("ALL")
            loadKuisFromFirebase("ALL") 
        }
        tabSelesai.setOnClickListener { 
            updateTabUI("SELESAI")
            loadKuisFromFirebase("SELESAI") 
        }
        tabBelum.setOnClickListener { 
            updateTabUI("BELUM")
            loadKuisFromFirebase("BELUM") 
        }
        
        updateTabUI("ALL")
    }

    private fun updateTabUI(filter: String) {
        currentFilter = filter
        val activeBg = R.drawable.bg_tab_active_new
        val inactiveBg = 0 // Transparent
        
        tabAll.setBackgroundResource(if (filter == "ALL") activeBg else inactiveBg)
        tabAll.setTextColor(if (filter == "ALL") ContextCompat.getColor(requireContext(), R.color.white) else ContextCompat.getColor(requireContext(), R.color.black))
        
        tabSelesai.setBackgroundResource(if (filter == "SELESAI") activeBg else inactiveBg)
        tabSelesai.setTextColor(if (filter == "SELESAI") ContextCompat.getColor(requireContext(), R.color.white) else ContextCompat.getColor(requireContext(), R.color.black))
        
        tabBelum.setBackgroundResource(if (filter == "BELUM") activeBg else inactiveBg)
        tabBelum.setTextColor(if (filter == "BELUM") ContextCompat.getColor(requireContext(), R.color.white) else ContextCompat.getColor(requireContext(), R.color.black))
    }

    private fun loadKuisFromFirebase(filter: String) {
        if (!isAdded) return
        progressBar.visibility = View.VISIBLE
        db.orderByChild("level").equalTo(userLevel.toDouble()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                progressBar.visibility = View.GONE
                val kuisList = mutableListOf<Kuis>()
                for (child in snapshot.children) {
                    try {
                        val map = child.value as? Map<*, *> ?: continue
                        val kuis = Kuis(
                            id = child.key ?: "",
                            judul = map["judul"]?.toString() ?: "",
                            aktif = map["aktif"] as? Boolean ?: true,
                            imageUrl = map["imageUrl"]?.toString() ?: "",
                            level = (map["level"] as? Long)?.toInt() ?: 1
                        )
                        if (kuis.aktif) kuisList.add(kuis)
                    } catch (e: Exception) { }
                }
                displayKuis(kuisList, filter)
            }
            override fun onCancelled(error: DatabaseError) { if (isAdded) progressBar.visibility = View.GONE }
        })
    }

    private fun displayKuis(list: List<Kuis>, filter: String) {
        if (!isAdded) return
        containerKuis.removeAllViews()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val pref = requireActivity().getSharedPreferences("KUIS_${userId}_LEVEL_$userLevel", Context.MODE_PRIVATE)
        
        val filteredList = when (filter) {
            "SELESAI" -> list.filter { pref.getBoolean("kuis_${it.id}_selesai", false) || pref.getBoolean("kuis_${it.id.replace("kuis_","")}_selesai", false) }
            "BELUM" -> list.filter { !pref.getBoolean("kuis_${it.id}_selesai", false) && !pref.getBoolean("kuis_${it.id.replace("kuis_","")}_selesai", false) }
            else -> list
        }

        if (filteredList.isEmpty()) { tvEmpty.visibility = View.VISIBLE; return }
        tvEmpty.visibility = View.GONE

        for (kuis in filteredList) {
            val cardView = LayoutInflater.from(requireContext()).inflate(R.layout.item_kuis_user_dynamic, containerKuis, false)
            val isSelesai = pref.getBoolean("kuis_${kuis.id}_selesai", false) || pref.getBoolean("kuis_${kuis.id.replace("kuis_","")}_selesai", false)
            val skor = pref.getInt("kuis_${kuis.id}_skor", 0).let { if (it == 0) pref.getInt("kuis_${kuis.id.replace("kuis_","")}_skor", 0) else it }

            val title = cardView.findViewById<TextView>(R.id.tvTitle)
            val status = cardView.findViewById<TextView>(R.id.tvStatus)
            val image = cardView.findViewById<ImageView>(R.id.imgQuiz)
            val btnAction = cardView.findViewById<Button>(R.id.btnAction)
            val btnTips = cardView.findViewById<Button>(R.id.btnTips)

            title.text = kuis.judul
            
            // --- LOAD GAMBAR: Prioritaskan Input Admin (imageUrl) ---
            if (kuis.imageUrl.isNotEmpty() && kuis.imageUrl != "img_lingkungan") {
                if (kuis.imageUrl.length > 100) {
                    // Base64 Image
                    try {
                        val bytes = Base64.decode(kuis.imageUrl, Base64.DEFAULT)
                        Glide.with(this).asBitmap().load(bytes).placeholder(R.drawable.img_lingkungan).into(image)
                    } catch (e: Exception) {
                        image.setImageResource(R.drawable.img_lingkungan)
                    }
                } else {
                    // Resource Name
                    val resId = resources.getIdentifier(kuis.imageUrl, "drawable", activity?.packageName)
                    image.setImageResource(if (resId != 0) resId else R.drawable.img_lingkungan)
                }
            } else {
                // Fallback ke Judul jika imageUrl kosong/default
                val judulLower = kuis.judul.lowercase()
                when {
                    judulLower.contains("air") -> image.setImageResource(R.drawable.img_air)
                    judulLower.contains("sampah") -> image.setImageResource(R.drawable.img_sampah)
                    judulLower.contains("peduli") -> image.setImageResource(R.drawable.gambar_peduli_lingkungan)
                    else -> image.setImageResource(R.drawable.img_lingkungan)
                }
            }

            if (isSelesai) {
                status.text = "Status: Selesai"
                status.setTextColor(ContextCompat.getColor(requireContext(), R.color.nav_active))
                btnAction.text = "Hasil ✓"
                btnAction.setOnClickListener {
                    val fragment = QuizMenang1Fragment()
                    fragment.arguments = Bundle().apply { 
                        putString("KUIS_ID", kuis.id); putInt("SKOR", skor); putInt("LEVEL", userLevel)
                    }
                    parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
                }
                if (skor >= 75) {
                    btnTips.visibility = View.VISIBLE
                    btnTips.setOnClickListener {
                        val fragmentTips = when(userLevel) {
                            1 -> TipsPeduliFragment(); 2 -> TipsSampahFragment()
                            3 -> TipsHematAirFragment(); else -> TipsFragment()
                        }
                        parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragmentTips).addToBackStack(null).commit()
                    }
                }
            } else {
                status.text = "Status: Belum Dikerjakan"
                status.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_button))
                btnAction.text = "Kerjakan"
                btnAction.setOnClickListener {
                    val fragment = when {
                        userLevel == 1 && kuis.judul.contains("Sampah", ignoreCase = true) -> QuizSoal2Fragment.newInstance(userLevel)
                        userLevel == 1 && kuis.judul.contains("Air", ignoreCase = true) -> QuizSoal3Fragment.newInstance(userLevel)
                        else -> QuizSoalFragment.newInstance(kuis.id, userLevel)
                    }
                    parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
                }
            }
            containerKuis.addView(cardView)
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
        loadKuisFromFirebase(currentFilter)
    }
}
