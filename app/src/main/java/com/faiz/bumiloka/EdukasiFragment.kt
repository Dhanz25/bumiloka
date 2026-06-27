package com.faiz.bumiloka

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.faiz.bumiloka.model.Edukasi
import com.google.firebase.database.*

class EdukasiFragment : Fragment() {

    private var userLevel = 1
    private lateinit var containerMateri: LinearLayout
    private lateinit var tvEdukasiLevel: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private val db = FirebaseDatabase.getInstance().reference.child("edukasi")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edukasi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        tvEdukasiLevel = view.findViewById(R.id.tvEdukasiLevel)
        containerMateri = view.findViewById(R.id.containerMateri)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        LevelHelper.getCurrentLevel(requireContext()) { level ->
            userLevel = level
            val levelName = when (level) {
                1 -> "Eco Beginner"
                2 -> "Eco Warrior"
                3 -> "Nature Protector"
                else -> "Eco Beginner"
            }
            tvEdukasiLevel.text = "Level $level ($levelName)"
            loadEdukasiFromFirebase(level)
        }
    }

    private fun loadEdukasiFromFirebase(level: Int) {
        progressBar.visibility = View.VISIBLE
        db.orderByChild("level").equalTo(level.toDouble()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                progressBar.visibility = View.GONE
                
                val edukasiList = mutableListOf<Edukasi>()
                for (child in snapshot.children) {
                    child.getValue(Edukasi::class.java)?.let {
                        it.id = child.key ?: ""
                        if (it.aktif) edukasiList.add(it)
                    }
                }

                updateUI(edukasiList)
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun updateUI(list: List<Edukasi>) {
        containerMateri.removeAllViews()
        
        if (list.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            return
        }
        
        tvEmpty.visibility = View.GONE
        val inflater = LayoutInflater.from(requireContext())

        for (data in list) {
            val cardView = inflater.inflate(R.layout.item_materi_lingkungan, containerMateri, false)
            
            val title = cardView.findViewById<TextView>(R.id.tvNama)
            val deskripsi = cardView.findViewById<TextView>(R.id.tvDeskripsi)
            val image = cardView.findViewById<ImageView>(R.id.imgMateri)
            
            title.text = data.title
            deskripsi.text = data.description
            
            // Perbaikan pemuatan gambar untuk mendukung Galeri (Base64) dan Drawable
            if (!data.imageUrl.isNullOrEmpty()) {
                if (data.imageUrl.length > 100) {
                    // Jika teks panjang, asumsikan Base64 dari galeri
                    try {
                        val imageBytes = Base64.decode(data.imageUrl, Base64.DEFAULT)
                        Glide.with(this)
                            .asBitmap()
                            .load(imageBytes)
                            .placeholder(R.drawable.img_lingkungan)
                            .into(image)
                    } catch (e: Exception) {
                        image.setImageResource(R.drawable.img_lingkungan)
                    }
                } else {
                    // Jika teks pendek, asumsikan nama drawable
                    val resId = resources.getIdentifier(data.imageUrl, "drawable", requireContext().packageName)
                    if (resId != 0) {
                        image.setImageResource(resId)
                    } else {
                        image.setImageResource(R.drawable.img_lingkungan)
                    }
                }
            } else {
                image.setImageResource(R.drawable.img_lingkungan)
            }

            cardView.setOnClickListener { bukaMateri(data.id) }
            containerMateri.addView(cardView)
        }
    }

    private fun bukaMateri(edukasiId: String) {
        val dariTantangan = arguments?.getBoolean("DARI_TANTANGAN", false) ?: false
        val fragment = MateriFragment()
        val args = Bundle()
        args.putString("edukasi_id", edukasiId)
        args.putBoolean("DARI_TANTANGAN", dariTantangan)
        fragment.arguments = args
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
    }
}
