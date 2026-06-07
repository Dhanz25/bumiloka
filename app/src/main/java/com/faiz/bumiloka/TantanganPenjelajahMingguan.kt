package com.faiz.bumiloka

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

    companion object {
        const val TOTAL_MATERI = 3
        const val TOTAL_KUIS = 3
    }

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

    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    private val prefName get() = "TANTANGAN_PENJELAJAH_$userId"
    private fun pref() = requireActivity().getSharedPreferences(prefName, Context.MODE_PRIVATE)

    private fun getMateriSelesai() = pref().getInt("materi_selesai", 0)
    private fun getKuisSelesai() = pref().getInt("kuis_selesai", 0)
    private fun isTantanganSelesai() = pref().getBoolean("tantangan_selesai", false)

    private fun tambahMateri() {
        val current = getMateriSelesai()
        if (current < TOTAL_MATERI) {
            pref().edit().putInt("materi_selesai", current + 1).apply()
        }
    }

    private fun tambahKuis() {
        val current = getKuisSelesai()
        if (current < TOTAL_KUIS) {
            pref().edit().putInt("kuis_selesai", current + 1).apply()
        }
    }

    private fun selesaikanTantangan() {
        pref().edit().putBoolean("tantangan_selesai", true).apply()
    }

    private fun hitungProgress(): Int {
        val pMateri = (getMateriSelesai().toFloat() / TOTAL_MATERI) * 50
        val pKuis = (getKuisSelesai().toFloat() / TOTAL_KUIS) * 50
        return (pMateri + pKuis).toInt()
    }

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

        // ✅ Listener dipasang SEKALI di sini
        parentFragmentManager.setFragmentResultListener(
            "materi_selesai_result", viewLifecycleOwner
        ) { _, _ ->
            tambahMateri()
            tambahKuis()
            updateUI()
            Toast.makeText(requireContext(),
                "Materi & Kuis ${getKuisSelesai()}/$TOTAL_KUIS selesai! ✅",
                Toast.LENGTH_SHORT).show()
        }

        parentFragmentManager.setFragmentResultListener(
            "kuis_selesai_result", viewLifecycleOwner
        ) { _, bundle ->
            val skor = bundle.getInt("skor", 0)
            tambahKuis()
            updateUI()
            Toast.makeText(requireContext(),
                "Kuis selesai! Skor: $skor ✅",
                Toast.LENGTH_SHORT).show()
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnMulaiMateri.setOnClickListener {
            if (getMateriSelesai() >= TOTAL_MATERI) {
                Toast.makeText(requireContext(), "Semua materi sudah selesai!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nextMateriId = getMateriSelesai() + 1

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MateriFragment.newInstance(nextMateriId))
                .addToBackStack(null)
                .commit()
        }

        btnMulaiKuis.setOnClickListener {
            if (getMateriSelesai() < TOTAL_MATERI) {
                Toast.makeText(requireContext(),
                    "Selesaikan semua materi terlebih dahulu!",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (getKuisSelesai() >= TOTAL_KUIS) {
                Toast.makeText(requireContext(), "Kuis sudah selesai semua!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QuizSoalFragment.newInstance(getKuisSelesai() + 1))
                .addToBackStack(null)
                .commit()
        }

        btnSelesai.setOnClickListener {
            val progress = hitungProgress()
            if (progress < 100) {
                Toast.makeText(requireContext(),
                    "Progress belum 100%! Saat ini: $progress%",
                    Toast.LENGTH_SHORT).show()
            } else {
                selesaikanTantangan()
                Toast.makeText(requireContext(),
                    "🎉 Tantangan Penjelajah Mingguan berhasil diselesaikan!",
                    Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottom_navigation).visibility = View.VISIBLE
    }

    private fun updateUI() {
        val materiSelesai = getMateriSelesai()
        val kuisSelesai = getKuisSelesai()
        val progress = hitungProgress()
        val selesai = isTantanganSelesai()

        tvProgressMateri.text = "$materiSelesai/$TOTAL_MATERI Progress selesai"
        pbMateri.progress = ((materiSelesai.toFloat() / TOTAL_MATERI) * 100).toInt()

        tvProgressKuis.text = "$kuisSelesai/$TOTAL_KUIS Progress selesai"
        pbKuis.progress = ((kuisSelesai.toFloat() / TOTAL_KUIS) * 100).toInt()

        tvProgressUtama.text = "Progress $progress%"
        pbUtama.progress = progress

        if (materiSelesai >= TOTAL_MATERI) {
            btnMulaiMateri.text = "Selesai ✓"
            btnMulaiMateri.isEnabled = false
        } else {
            btnMulaiMateri.text = "Mulai"
            btnMulaiMateri.isEnabled = true
        }

        if (kuisSelesai >= TOTAL_KUIS) {
            btnMulaiKuis.text = "Selesai ✓"
            btnMulaiKuis.isEnabled = false
        } else {
            btnMulaiKuis.text = "Mulai"
            btnMulaiKuis.isEnabled = materiSelesai >= TOTAL_MATERI
        }

        if (selesai || progress >= 100) {
            btnSelesai.text = "Tantangan Selesai ✓"
            btnSelesai.isEnabled = false
        } else {
            btnSelesai.text = "Selesai"
            btnSelesai.isEnabled = true
        }
    }
}