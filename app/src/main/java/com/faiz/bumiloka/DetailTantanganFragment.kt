package com.faiz.bumiloka

import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailTantanganFragment : Fragment(R.layout.fragment_detailtantangan) {

    private lateinit var tvJudul: TextView
    private lateinit var tvDeskripsi: TextView
    private lateinit var ivThumbnail: ImageView
    private lateinit var btnMulaiMateri: Button
    private lateinit var btnMulaiKuis: Button
    private lateinit var btnSelesai: Button
    private lateinit var tvProgressUtama: TextView
    private lateinit var progressBarUtama: ProgressBar
    
    private lateinit var tvProgressMateri: TextView
    private lateinit var progressBarMateri: ProgressBar
    private lateinit var tvProgressKuis: TextView
    private lateinit var progressBarKuis: ProgressBar

    private var challengeId: String? = null
    private var materiId: String? = null
    private var quizId: String? = null
    private var badgeId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind Views
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        tvJudul = view.findViewById(R.id.tv_judul_detail)
        tvDeskripsi = view.findViewById(R.id.tv_deskripsi_detail)
        ivThumbnail = view.findViewById(R.id.iv_thumbnail_detail)
        btnMulaiMateri = view.findViewById(R.id.btnMulaiMateri)
        btnMulaiKuis = view.findViewById(R.id.btnMulaiKuis)
        btnSelesai = view.findViewById(R.id.btnSelesai)
        
        tvProgressUtama = view.findViewById(R.id.tvProgressUtama)
        progressBarUtama = view.findViewById(R.id.progressBarUtama)
        
        tvProgressMateri = view.findViewById(R.id.tvProgressMateri)
        progressBarMateri = view.findViewById(R.id.progressBarMateri)
        
        tvProgressKuis = view.findViewById(R.id.tvProgressKuis)
        progressBarKuis = view.findViewById(R.id.progressBarKuis)

        // Get Data
        arguments?.let {
            challengeId = it.getString("id")
            materiId = it.getString("materiId")
            quizId = it.getString("quizId")
            badgeId = it.getString("badgeId")
            
            view.findViewById<TextView>(R.id.tv_judul_header_tantangan)?.text = it.getString("judul")
            tvJudul.text = it.getString("judul")
            tvDeskripsi.text = it.getString("deskripsi")
            
            val img = it.getString("imageUrl") ?: ""
            if (img.length > 100) {
                try {
                    val imageBytes = Base64.decode(img, Base64.DEFAULT)
                    Glide.with(this).asBitmap().load(imageBytes).into(ivThumbnail)
                } catch (e: Exception) {
                    ivThumbnail.setImageResource(R.drawable.img_lingkungan)
                }
            } else {
                val resId = resources.getIdentifier(img, "drawable", requireContext().packageName)
                ivThumbnail.setImageResource(if (resId != 0) resId else R.drawable.img_lingkungan)
            }
        }

        loadProgress()

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnMulaiMateri.setOnClickListener {
            materiId?.let { id ->
                val fragment = MateriFragment.newInstanceForChallenge(id)
                val bundle = fragment.arguments ?: Bundle()
                bundle.putString("challenge_id", challengeId)
                bundle.putString("quiz_id", quizId)
                bundle.putString("badge_id", badgeId)
                fragment.arguments = bundle
                
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        btnMulaiKuis.setOnClickListener {
            quizId?.let { id ->
                // Periksa apakah materi sudah selesai (bisa ditambah logic di sini)
                val fragment = QuizSoalFragment.newInstance(id, 1)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        btnSelesai.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadProgress() {
        // Simple mock progress for now, as real progress depends on other fragments
        val isMateriDone = false // Implement real check if needed
        val isKuisDone = false // Implement real check if needed
        
        var totalProgress = 0
        if (isMateriDone) totalProgress += 50
        if (isKuisDone) totalProgress += 50
        
        tvProgressUtama.text = "Progress $totalProgress%"
        progressBarUtama.progress = totalProgress
        
        progressBarMateri.progress = if (isMateriDone) 100 else 0
        tvProgressMateri.text = if (isMateriDone) "1/1 Progress selesai" else "0/1 Progress selesai"
        
        progressBarKuis.progress = if (isKuisDone) 100 else 0
        tvProgressKuis.text = if (isKuisDone) "1/1 Progress selesai" else "0/1 Progress selesai"
    }

    companion object {
        fun newInstance(id: String, judul: String, deskripsi: String, imageUrl: String, materiId: String, quizId: String, badgeId: String = ""): DetailTantanganFragment {
            return DetailTantanganFragment().apply {
                arguments = Bundle().apply {
                    putString("id", id)
                    putString("judul", judul)
                    putString("deskripsi", deskripsi)
                    putString("imageUrl", imageUrl)
                    putString("materiId", materiId)
                    putString("quizId", quizId)
                    putString("badgeId", badgeId)
                }
            }
        }
    }
}