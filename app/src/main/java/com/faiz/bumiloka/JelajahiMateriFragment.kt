package com.faiz.bumiloka

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class JelajahiMateriFragment :
    Fragment(R.layout.fragment_jelajahi_materi) {

    private var countDownTimer: CountDownTimer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnLanjut = view.findViewById<Button>(R.id.btnLanjut)

        // 🔒 awal disable
        btnLanjut.isEnabled = false
        btnLanjut.text = "Tunggu..."

        btnLanjut.backgroundTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.darker_gray
                )
            )

        // ===============================
        // TIMER
        // ===============================
        countDownTimer = object : CountDownTimer(10000, 1000) {

            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {

                // ✅ cegah crash
                if (!isAdded) return

                btnLanjut.isEnabled = true
                btnLanjut.text = "Lanjut"

                btnLanjut.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.nav_active
                        )
                    )
            }

        }.start()

        // ===============================
        // BACK BUTTON ATAS
        // ===============================
        btnBack.setOnClickListener {
            showExitDialog()
        }

        // ===============================
        // BACK HP
        // ===============================
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitDialog()
                }
            }
        )

        // ===============================
        // LANJUT
        // ===============================
        btnLanjut.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    Jelajahi_MateriDetail()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    // ===============================
    // POPUP KELUAR
    // ===============================
    private fun showExitDialog() {

        val dialogView = layoutInflater.inflate(
            R.layout.popup_keluarmisi,
            null
        )

        val btnBatal =
            dialogView.findViewById<Button>(R.id.btnBatal)

        val btnKeluar =
            dialogView.findViewById<Button>(R.id.btnKeluar)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnKeluar.setOnClickListener {

            // ✅ stop timer sebelum keluar
            countDownTimer?.cancel()

            dialog.dismiss()

            parentFragmentManager.popBackStack()
        }

        dialog.show()
    }

    // ===============================
    // DESTROY VIEW
    // ===============================
    override fun onDestroyView() {
        super.onDestroyView()

        // ✅ wajib supaya tidak crash
        countDownTimer?.cancel()
    }
}