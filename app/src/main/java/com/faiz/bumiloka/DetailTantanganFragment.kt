package com.faiz.bumiloka

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.faiz.bumiloka.model.Tantangan
import com.google.firebase.auth.FirebaseAuth

class DetailTantanganFragment : Fragment(R.layout.fragment_detailtantangan) {

    private lateinit var tvJudul: TextView
    private lateinit var tvDeskripsi: TextView
    private lateinit var ivThumbnail: ImageView
    private lateinit var btnSelesai: Button
    private lateinit var tvProgressUtama: TextView
    private lateinit var progressBarUtama: ProgressBar
    
    private lateinit var cardMateri: View
    private lateinit var tvProgressMateri: TextView
    private lateinit var progressBarMateri: ProgressBar
    private lateinit var btnMulaiMateri: Button
    private lateinit var tvDescTaskMateri: TextView

    private lateinit var cardKuis: View
    private lateinit var tvProgressKuis: TextView
    private lateinit var progressBarKuis: ProgressBar
    private lateinit var btnMulaiKuis: Button
    private lateinit var tvDescTaskKuis: TextView

    private var challengeId: String? = null
    private var materiId: String? = null
    private var quizId: String? = null
    private var badgeId: String? = null
    private var type: String = "SINGLE"
    private var targetCount: Int = 1
    private var userLevel: Int = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

        initViews(view)
        setupData()
        loadProgress()

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun initViews(view: View) {
        tvJudul = view.findViewById(R.id.tv_judul_detail)
        tvDeskripsi = view.findViewById(R.id.tv_deskripsi_detail)
        ivThumbnail = view.findViewById(R.id.iv_thumbnail_detail)
        btnSelesai = view.findViewById(R.id.btnSelesai)
        tvProgressUtama = view.findViewById(R.id.tvProgressUtama)
        progressBarUtama = view.findViewById(R.id.progressBarUtama)

        cardMateri = view.findViewById(R.id.cardTaskMateri)
        tvProgressMateri = view.findViewById(R.id.tvProgressMateri)
        progressBarMateri = view.findViewById(R.id.progressBarMateri)
        btnMulaiMateri = view.findViewById(R.id.btnMulaiMateri)
        tvDescTaskMateri = view.findViewById(R.id.tvDescTaskMateri)

        cardKuis = view.findViewById(R.id.cardTaskKuis)
        tvProgressKuis = view.findViewById(R.id.tvProgressKuis)
        progressBarKuis = view.findViewById(R.id.progressBarKuis)
        btnMulaiKuis = view.findViewById(R.id.btnMulaiKuis)
        tvDescTaskKuis = view.findViewById(R.id.tvDescTaskKuis)
    }

    private fun setupData() {
        arguments?.let {
            challengeId = it.getString("id")
            materiId = it.getString("materiId")
            quizId = it.getString("quizId")
            badgeId = it.getString("badgeId")
            type = it.getString("type", "SINGLE")
            targetCount = it.getInt("targetCount", 1)
            userLevel = it.getInt("level", 1)
            
            view?.findViewById<TextView>(R.id.tv_judul_header_tantangan)?.text = it.getString("judul")
            tvJudul.text = it.getString("judul")
            tvDeskripsi.text = it.getString("deskripsi")
            
            val img = it.getString("imageUrl") ?: ""
            if (img.isNotEmpty()) {
                if (img.length > 100 || img.startsWith("http")) {
                    val source = if (img.length > 100 && !img.startsWith("http")) 
                        Base64.decode(img, Base64.DEFAULT) else img
                    Glide.with(this).load(source).placeholder(R.drawable.tantangan1).into(ivThumbnail)
                } else {
                    val resId = resources.getIdentifier(img, "drawable", activity?.packageName)
                    ivThumbnail.setImageResource(if (resId != 0) resId else R.drawable.tantangan1)
                }
            }
        }

        when (type) {
            "SINGLE" -> {
                cardMateri.visibility = if (materiId.isNullOrEmpty()) View.GONE else View.VISIBLE
                cardKuis.visibility = if (quizId.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            "QUIZ_COUNT" -> {
                cardMateri.visibility = View.GONE
                cardKuis.visibility = View.VISIBLE
                btnMulaiKuis.text = "Ke Daftar Kuis"
            }
            "MATERI_COUNT" -> {
                cardMateri.visibility = View.VISIBLE
                cardKuis.visibility = View.GONE
                btnMulaiMateri.text = "Ke Daftar Materi"
            }
        }

        btnMulaiMateri.setOnClickListener {
            if (!isAdded) return@setOnClickListener
            if (type == "MATERI_COUNT") {
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, EdukasiFragment()).addToBackStack(null).commit()
            } else {
                materiId?.split(",")?.firstOrNull { it.isNotBlank() }?.let { id ->
                    val fragment = MateriFragment.newInstance(id.trim(), userLevel)
                    parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
                }
            }
        }

        btnMulaiKuis.setOnClickListener {
            if (!isAdded) return@setOnClickListener
            if (type == "QUIZ_COUNT") {
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, QuizUtamaFragment.newInstance(userLevel)).addToBackStack(null).commit()
            } else {
                quizId?.split(",")?.firstOrNull { it.isNotBlank() }?.let { id ->
                    val fragment = QuizSoalFragment.newInstance(id.trim(), userLevel)
                    parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
                }
            }
        }
    }

    private fun loadProgress() {
        if (!isAdded) return
        val ctx = context ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val tantanganPref = ctx.getSharedPreferences("TANTANGAN_STATUS_$userId", Context.MODE_PRIVATE)
        val kuisPref = ctx.getSharedPreferences("KUIS_${userId}_LEVEL_$userLevel", Context.MODE_PRIVATE)
        
        var totalProgress = 0
        val isSelesai = challengeId?.let { TantanganStatusHelper.isTantanganSelesai(ctx, it) } ?: false

        if (isSelesai) {
            totalProgress = 100
        } else {
            // Logika hitung progress... (disederhanakan untuk stabilitas)
            totalProgress = 0 
        }

        tvProgressUtama.text = "Progress $totalProgress%"
        progressBarUtama.progress = totalProgress

        if (isSelesai) {
            btnSelesai.text = "Tantangan Selesai ✓"
            btnSelesai.isEnabled = false
        } else if (totalProgress >= 100) {
            btnSelesai.isEnabled = true
            btnSelesai.setOnClickListener {
                challengeId?.let { id ->
                    TantanganStatusHelper.setTantanganSelesai(ctx, id, materiId ?: "", quizId ?: "", 100)
                    loadProgress()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }

    companion object {
        fun newInstance(t: Tantangan): DetailTantanganFragment = DetailTantanganFragment().apply {
            arguments = Bundle().apply {
                putString("id", t.id); putString("judul", t.judul); putString("deskripsi", t.deskripsi)
                putString("imageUrl", t.imageUrl); putString("materiId", t.materiId); putString("quizId", t.quizId)
                putString("badgeId", t.badgeId); putString("type", t.type); putInt("targetCount", t.targetCount); putInt("level", t.level)
            }
        }
    }
}
