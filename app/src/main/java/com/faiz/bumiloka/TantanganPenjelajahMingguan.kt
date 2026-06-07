package com.faiz.bumiloka

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class TantanganPenjelajahMingguanFragment : Fragment(R.layout.fragment_tantangan_penjelajah_mingguan) {

    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    private val prefName get() = "TANTANGAN_PENJELAJAH_$userId"
    private fun pref() = requireActivity().getSharedPreferences(prefName, Context.MODE_PRIVATE)

    private fun isMateriSelesai(id: Int) = pref().getBoolean("materi${id}_selesai", false)
    private fun setMateriSelesai(id: Int) = pref().edit().putBoolean("materi${id}_selesai", true).apply()
    private fun getMateriSelesaiCount() = (1..3).count { isMateriSelesai(it) }
    private fun isSemuaSelesai() = getMateriSelesaiCount() == 3
    private fun isPopupShown() = pref().getBoolean("popup_shown", false)
    private fun setPopupShown() = pref().edit().putBoolean("popup_shown", true).apply()

    private lateinit var btnBack: View
    private lateinit var btnMulaiMateri: Button
    private lateinit var btnMulaiKuis: Button
    private lateinit var btnSelesai: Button
    private lateinit var tvProgressMateri: TextView
    private lateinit var pbMateri: ProgressBar
    private lateinit var tvProgressKuis: TextView
    private lateinit var pbKuis: ProgressBar
    private lateinit var tvProgressUtama: TextView
    private lateinit var pbUtama: ProgressBar

    // Menyimpan materi mana yang sedang dibuka
    private var materiYangDibuka = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<View>(R.id.bottom_navigation).visibility = View.GONE

        btnBack = view.findViewById(R.id.btnBack)
        btnMulaiMateri = view.findViewById(R.id.btnMulaiMateri)
        btnMulaiKuis = view.findViewById(R.id.btnMulaiKuis)
        btnSelesai = view.findViewById(R.id.btnSelesai)
        tvProgressMateri = view.findViewById(R.id.tvProgressMateri)
        pbMateri = view.findViewById(R.id.progressBarMateri)
        tvProgressKuis = view.findViewById(R.id.tvProgressKuis)
        pbKuis = view.findViewById(R.id.progressBarKuis)
        tvProgressUtama = view.findViewById(R.id.tvProgressUtama)
        pbUtama = view.findViewById(R.id.progressBarUtama)

        updateUI()

        // ✅ Listener dipasang SEKALI — materi selesai = kuis ikut naik
        parentFragmentManager.setFragmentResultListener(
            "materi_selesai_result", viewLifecycleOwner
        ) { _, bundle ->
            val materiId = bundle.getInt("materi_id", materiYangDibuka)
            if (materiId > 0) setMateriSelesai(materiId)
            updateUI()
            cekSemuaSelesai()
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // ✅ Tombol Mulai Materi — pilih materi yang belum selesai
        btnMulaiMateri.setOnClickListener {
            if (getMateriSelesaiCount() >= 3) {
                Toast.makeText(requireContext(), "Semua materi sudah selesai!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val fragment = EdukasiFragment()
            val args = Bundle().apply {
                putBoolean("DARI_TANTANGAN", true) // ✅ tambah ini
            }
            fragment.arguments = args
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        btnMulaiKuis.setOnClickListener {
            if (getMateriSelesaiCount() >= 3) {
                Toast.makeText(requireContext(), "Semua kuis sudah selesai!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EdukasiFragment())
                .addToBackStack(null)
                .commit()
        }

        btnSelesai.setOnClickListener {
            if (!isSemuaSelesai()) {
                Toast.makeText(requireContext(),
                    "Selesaikan semua materi terlebih dahulu!",
                    Toast.LENGTH_SHORT).show()
            } else {
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        cekSemuaSelesai()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottom_navigation).visibility = View.VISIBLE
    }

    private fun cekSemuaSelesai() {
        if (isSemuaSelesai() && !isPopupShown()) {
            setPopupShown()
            showPopupSelesai()
        }
    }

    private fun showPopupSelesai() {
        val dialogView = layoutInflater.inflate(R.layout.popup_tantanganselesai1, null)
        val tvJudul = dialogView.findViewById<TextView>(R.id.tvJudulPopup)
        val btnLanjut = dialogView.findViewById<Button>(R.id.btnLanjutPopup)

        tvJudul?.text = "Penjelajah Mingguan Selesai!"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnLanjut.setOnClickListener {
            dialog.dismiss()
            parentFragmentManager.popBackStack()
        }

        dialog.show()
    }

    private fun updateUI() {
        val materiSelesai = getMateriSelesaiCount()
        val progress = (materiSelesai.toFloat() / 3 * 100).toInt()

        // Progress utama
        tvProgressUtama.text = "Progress $progress%"
        pbUtama.progress = progress

        // Progress Materi
        tvProgressMateri.text = "$materiSelesai/3 Progress selesai"
        pbMateri.progress = (materiSelesai.toFloat() / 3 * 100).toInt()

        // Progress Kuis (sama dengan materi karena materi + kuis 1 paket)
        tvProgressKuis.text = "$materiSelesai/3 Progress selesai"
        pbKuis.progress = (materiSelesai.toFloat() / 3 * 100).toInt()

        // Tombol Mulai Materi
        if (materiSelesai >= 3) {
            btnMulaiMateri.text = "Selesai ✓"
            btnMulaiMateri.isEnabled = false
        } else {
            btnMulaiMateri.text = "Mulai"
            btnMulaiMateri.isEnabled = true
        }

        // Tombol Mulai Kuis
        if (materiSelesai >= 3) {
            btnMulaiKuis.text = "Selesai ✓"
            btnMulaiKuis.isEnabled = false
        } else {
            btnMulaiKuis.text = "Mulai"
            btnMulaiKuis.isEnabled = true
        }

        // Tombol Selesai
        btnSelesai.isEnabled = isSemuaSelesai()
    }
}
