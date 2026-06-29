package com.faiz.bumiloka

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
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
    private var userLevel = 1
    private val db = FirebaseDatabase.getInstance().getReference("kuis")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        tvLevelIndicator = view.findViewById(R.id.tvLevelIndicator)
        containerKuis = view.findViewById(R.id.containerKuis)
        progressBar = view.findViewById(R.id.progressBarKuis)
        tvEmpty = view.findViewById(R.id.tvEmptyKuis)

        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        val tabAll = view.findViewById<TextView>(R.id.tab_all)
        val tabSelesai = view.findViewById<TextView>(R.id.tab_selesai)
        val tabBelum = view.findViewById<TextView>(R.id.tab_belum)

        LevelHelper.getCurrentLevel(requireContext()) { level ->
            userLevel = level
            val levelName = when (level) {
                1 -> "Eco Beginner"
                2 -> "Eco Warrior"
                3 -> "Nature Protector"
                else -> "Eco Beginner"
            }
            tvLevelIndicator.text = "Level $level ($levelName)"
            loadKuisFromFirebase("ALL")
        }

        tabAll.setOnClickListener { 
            setActiveTab(tabAll, tabSelesai, tabBelum)
            loadKuisFromFirebase("ALL") 
        }
        tabSelesai.setOnClickListener { 
            setActiveTab(tabSelesai, tabAll, tabBelum)
            loadKuisFromFirebase("SELESAI") 
        }
        tabBelum.setOnClickListener { 
            setActiveTab(tabBelum, tabAll, tabSelesai)
            loadKuisFromFirebase("BELUM") 
        }
    }

    private fun setActiveTab(active: TextView, vararg inactives: TextView) {
        active.setBackgroundResource(R.drawable.bg_tab_active_new)
        active.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        inactives.forEach { 
            it.setBackgroundResource(0)
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    private fun loadKuisFromFirebase(filter: String) {
        progressBar.visibility = View.VISIBLE
        db.orderByChild("level").equalTo(userLevel.toDouble()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                progressBar.visibility = View.GONE
                
                val kuisList = mutableListOf<Kuis>()
                for (child in snapshot.children) {
                    if (child.value is Map<*, *>) {
                        try {
                            child.getValue(Kuis::class.java)?.let {
                                it.id = child.key ?: ""
                                if (it.aktif) kuisList.add(it)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                displayKuis(kuisList, filter)
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) progressBar.visibility = View.GONE
            }
        })
    }

    private fun displayKuis(list: List<Kuis>, filter: String) {
        containerKuis.removeAllViews()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val pref = requireActivity().getSharedPreferences("KUIS_${userId}_LEVEL_$userLevel", Context.MODE_PRIVATE)
        
        val filteredList = when (filter) {
            "SELESAI" -> list.filter { pref.getBoolean("kuis_${it.id}_selesai", false) }
            "BELUM" -> list.filter { !pref.getBoolean("kuis_${it.id}_selesai", false) }
            else -> list
        }

        if (filteredList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            return
        }
        tvEmpty.visibility = View.GONE

        val inflater = LayoutInflater.from(requireContext())
        for (kuis in filteredList) {
            val cardView = inflater.inflate(R.layout.item_kuis_user_dynamic, containerKuis, false)
            
            val title = cardView.findViewById<TextView>(R.id.tvTitle)
            val status = cardView.findViewById<TextView>(R.id.tvStatus)
            val image = cardView.findViewById<ImageView>(R.id.imgQuiz)
            val btnAction = cardView.findViewById<Button>(R.id.btnAction)
            val btnTips = cardView.findViewById<Button>(R.id.btnTips)

            title.text = kuis.judul
            val isSelesai = pref.getBoolean("kuis_${kuis.id}_selesai", false)
            val skor = pref.getInt("kuis_${kuis.id}_skor", 0)

            // Load Image
            if (kuis.imageUrl.isNotEmpty()) {
                if (kuis.imageUrl.length > 100) {
                    try {
                        val imageBytes = Base64.decode(kuis.imageUrl, Base64.DEFAULT)
                        Glide.with(this).asBitmap().load(imageBytes).into(image)
                    } catch (e: Exception) { image.setImageResource(R.drawable.img_lingkungan) }
                } else {
                    val resId = resources.getIdentifier(kuis.imageUrl, "drawable", requireContext().packageName)
                    image.setImageResource(if (resId != 0) resId else R.drawable.img_lingkungan)
                }
            } else {
                image.setImageResource(R.drawable.img_lingkungan)
            }

            if (isSelesai) {
                status.text = "Status: Selesai"
                status.setTextColor(ContextCompat.getColor(requireContext(), R.color.nav_active))
                btnAction.text = "Selesai ✓"
                btnAction.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey_button))
                btnAction.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_subtitle))
                
                btnAction.setOnClickListener {
                    // Review result
                    val fragment = QuizMenang1Fragment()
                    fragment.arguments = Bundle().apply { 
                        putString("KUIS_ID", kuis.id)
                        putInt("SKOR", skor)
                    }
                    parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
                }

                if (skor == 100) {
                    btnTips.visibility = View.VISIBLE
                    btnTips.setOnClickListener {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, TipsFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                } else {
                    btnTips.visibility = View.GONE
                }
            } else {
                status.text = "Status: Belum Dikerjakan"
                status.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_button))
                btnAction.text = "Kerjakan"
                btnAction.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.nav_active))
                btnAction.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                btnAction.setOnClickListener {
                    val fragment = QuizSoalFragment.newInstance(kuis.id, userLevel)
                    parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
                }
                btnTips.visibility = View.GONE
            }

            containerKuis.addView(cardView)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }
}
