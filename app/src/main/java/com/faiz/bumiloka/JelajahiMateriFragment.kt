package com.faiz.bumiloka

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.faiz.bumiloka.model.Edukasi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JelajahiMateriFragment : Fragment(R.layout.fragment_jelajahi_materi) {

    private var countDownTimer: CountDownTimer? = null
    private var edukasiId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnLanjut = view.findViewById<Button>(R.id.btnLanjut)

        // 🔒 awal disable
        btnLanjut.isEnabled = false
        btnLanjut.text = "Tunggu..."
        btnLanjut.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))

        // Get ID from arguments or find the first material for the current level
        edukasiId = arguments?.getString("edukasi_id")
        
        if (edukasiId != null) {
            loadData(edukasiId!!)
        } else {
            // Find default material for current level
            LevelHelper.getCurrentLevel(requireContext()) { level ->
                findFirstMaterialForLevel(level)
            }
        }

        // ===============================
        // TIMER (10 Detik)
        // ===============================
        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (isAdded) btnLanjut.text = "Tunggu (${millisUntilFinished/1000}s)"
            }
            override fun onFinish() {
                if (!isAdded) return
                btnLanjut.isEnabled = true
                btnLanjut.text = "Lanjut"
                btnLanjut.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.nav_active))
            }
        }.start()

        btnBack.setOnClickListener { showExitDialog() }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { showExitDialog() }
        })

        btnLanjut.setOnClickListener {
            val fragment = Jelajahi_MateriDetail()
            val args = Bundle()
            args.putString("edukasi_id", edukasiId)
            fragment.arguments = args
            
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun findFirstMaterialForLevel(level: Int) {
        val db = FirebaseDatabase.getInstance().reference.child("edukasi")
        db.orderByChild("level").equalTo(level.toDouble()).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val child = snapshot.children.firstOrNull()
                    val data = child?.getValue(Edukasi::class.java)
                    if (data != null) {
                        edukasiId = child.key
                        displayData(data)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadData(id: String) {
        FirebaseDatabase.getInstance().reference.child("edukasi").child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(Edukasi::class.java)?.let { displayData(it) }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun displayData(edukasi: Edukasi) {
        val v = view ?: return
        v.findViewById<TextView>(R.id.tvJudulUtama)?.text = edukasi.title
        v.findViewById<TextView>(R.id.tvDescription)?.text = edukasi.description
        v.findViewById<TextView>(R.id.tvIsiTitle)?.text = edukasi.isiTitle
        v.findViewById<TextView>(R.id.tvIsiMateri)?.text = edukasi.content
        v.findViewById<TextView>(R.id.tvContohTitle)?.text = edukasi.contohTitle
        v.findViewById<TextView>(R.id.tvContohContent)?.text = edukasi.contohContent

        val imgMateri = v.findViewById<ImageView>(R.id.imgMateri)
        if (imgMateri != null && edukasi.imageUrl.isNotEmpty()) {
            val resId = resources.getIdentifier(edukasi.imageUrl, "drawable", requireContext().packageName)
            if (resId != 0) imgMateri.setImageResource(resId)
        }
    }

    private fun showExitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.popup_keluarmisi, null)
        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val btnKeluar = dialogView.findViewById<Button>(R.id.btnKeluar)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(false).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        btnBatal.setOnClickListener { dialog.dismiss() }
        btnKeluar.setOnClickListener {
            countDownTimer?.cancel()
            dialog.dismiss()
            parentFragmentManager.popBackStack()
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}
