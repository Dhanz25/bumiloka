package com.faiz.bumiloka

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.model.Edukasi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Jelajahi_MateriDetail : Fragment(R.layout.fragment_jelajahi_materi) {

    private var edukasiId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnLanjut = view.findViewById<Button>(R.id.btnLanjut)

        edukasiId = arguments?.getString("id") ?: arguments?.getString("edukasi_id")
        
        if (edukasiId != null) {
            loadData(edukasiId!!)
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnLanjut.setOnClickListener {
            completeMission()
        }
    }

    private fun loadData(id: String) {
        FirebaseDatabase.getInstance().reference.child("edukasi").child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    snapshot.getValue(Edukasi::class.java)?.let { displayData(it) }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun displayData(edukasi: Edukasi) {
        val v = view ?: return
        
        v.findViewById<TextView>(R.id.tvJudulUtama)?.text = edukasi.title
        v.findViewById<TextView>(R.id.tvDescription)?.text = if (edukasi.description.isNotEmpty()) edukasi.description else "Baca materi ini untuk menambah wawasanmu."

        v.findViewById<TextView>(R.id.tvIsiTitle)?.text = if (edukasi.section1Title.isNotEmpty()) edukasi.section1Title else edukasi.title
        v.findViewById<TextView>(R.id.tvIsiMateri)?.text = if (edukasi.section1Content.isNotEmpty()) edukasi.section1Content else edukasi.content

        val tvS2Title = v.findViewById<TextView>(R.id.tvContohTitle)
        val tvS2Content = v.findViewById<TextView>(R.id.tvContohContent)
        if (edukasi.section2Title.isNotEmpty()) {
            tvS2Title?.text = edukasi.section2Title
            tvS2Content?.text = edukasi.section2Content
            tvS2Title?.visibility = View.VISIBLE
            tvS2Content?.visibility = View.VISIBLE
        } else {
            tvS2Title?.visibility = View.GONE
            tvS2Content?.visibility = View.GONE
        }

        val tvS3Title = v.findViewById<TextView>(R.id.tvSection3Title)
        val tvS3Content = v.findViewById<TextView>(R.id.tvSection3Content)
        if (edukasi.section3Title.isNotEmpty()) {
            tvS3Title?.text = edukasi.section3Title
            tvS3Content?.text = edukasi.section3Content
            tvS3Title?.visibility = View.VISIBLE
            tvS3Content?.visibility = View.VISIBLE
        } else {
            tvS3Title?.visibility = View.GONE
            tvS3Content?.visibility = View.GONE
        }

        val imgMateri = v.findViewById<ImageView>(R.id.imgMateri)
        if (imgMateri != null && edukasi.imageUrl.isNotEmpty()) {
            if (edukasi.imageUrl.length > 150) {
                try {
                    val decodedString = Base64.decode(edukasi.imageUrl, Base64.DEFAULT)
                    val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    imgMateri.setImageBitmap(decodedByte)
                } catch (e: Exception) {
                    imgMateri.setImageResource(R.drawable.img_lingkungan)
                }
            } else {
                val resId = resources.getIdentifier(edukasi.imageUrl, "drawable", requireContext().packageName)
                imgMateri.setImageResource(if (resId != 0) resId else R.drawable.img_lingkungan)
            }
        }
    }

    private fun completeMission() {
        val ctx = requireContext()
        val id = edukasiId ?: ""
        
        // Mengurangi poin dari 30 menjadi 10 agar tidak terlalu cepat naik level
        AktivitasHelper.tambahPoint(ctx, 10, "Membaca Materi")
        AktivitasHelper.tambahMisiSelesai(ctx)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        LevelHelper.getCurrentLevel(ctx) { currentLevel ->
            TantanganStatusHelper.syncAllProgress(ctx, currentLevel, "MATERI", id)
            val prefMisi = requireActivity().getSharedPreferences("MISI_${userId}_LEVEL_$currentLevel", Context.MODE_PRIVATE)
            prefMisi.edit().putBoolean("misi1_selesai", true).apply()
            
            // Membuka gembok level berikutnya tanpa mengubah level aktif
            UnlockLevelHelper.checkAndUnlockNextLevel(ctx, currentLevel)
            showSuccessPopup()
        }
    }

    private fun showSuccessPopup() {
        if (!isAdded) return
        val viewDialog = layoutInflater.inflate(R.layout.pop_up_misiselesai, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(viewDialog)
            .setCancelable(false)
            .create()

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        viewDialog.findViewById<Button>(R.id.btnLanjutPopup).setOnClickListener {
            dialog.dismiss()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MisiFragment())
                .commit()
        }
    }
}
