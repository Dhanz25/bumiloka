package com.faiz.bumiloka

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class Jelajahi_MateriDetail : Fragment(R.layout.fragment_jelajahi__materi_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val checkSelesai = view.findViewById<CheckBox>(R.id.checkSelesai)
        val btnSelesai = view.findViewById<Button>(R.id.btnSelesai)

        // tombol kembali
        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // awal: tombol disable
        btnSelesai.isEnabled = false

        // checkbox logic
        checkSelesai.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                btnSelesai.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.nav_active)
                )
                btnSelesai.isEnabled = true
            } else {
                btnSelesai.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
                )
                btnSelesai.isEnabled = false
            }
        }

        // klik selesai
        btnSelesai.setOnClickListener {

            AktivitasManager.tambahAktivitas(
                requireContext(),
                "Menyelesaikan Misi Jelajahi Materi",
                "Misi",
                20
            )
            AktivitasHelper.tambahPoint(requireContext(), 30) // ← WAJIB (ini yang ngaruh ke progress bar)
            AktivitasHelper.tambahMisiSelesai() // ← opsional tapi bagus untuk tracking

            // simpan status misi selesai
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
            LevelHelper.getCurrentLevel(requireContext()) { currentLevel ->

                val prefMisi = requireActivity().getSharedPreferences(
                    "MISI_${userId}_LEVEL_$currentLevel",
                    Context.MODE_PRIVATE
                )

                prefMisi.edit()
                    .putBoolean("misi1_selesai", true)
                    .apply()
            }

            // ✅ Cek apakah level berikutnya terbuka
            LevelHelper.getCurrentLevel(requireContext()) { current ->
                UnlockLevelHelper.checkAndUnlockNextLevel(requireContext(), current)
            }

            // 🔥 pakai custom dialog
            val viewDialog = layoutInflater.inflate(R.layout.pop_up_misiselesai, null)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(viewDialog)
                .setCancelable(false)
                .create()

            dialog.show()

            // background transparan & dim
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.setDimAmount(0.6f)

            // 🎬 animasi popup
            val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_up_scale)
            viewDialog.startAnimation(anim)

            // tombol lanjut di popup
            val btnLanjutPopup = viewDialog.findViewById<Button>(R.id.btnLanjutPopup)

            btnLanjutPopup.setOnClickListener {

                dialog.dismiss()

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MisiFragment())
                    .commit()
            }
        }
    }
}