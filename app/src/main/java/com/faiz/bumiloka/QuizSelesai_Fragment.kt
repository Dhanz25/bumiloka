package com.faiz.bumiloka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class QuizSelesaiFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz_selesai_, container, false)
    }
    // Tambahkan bagian ini untuk memproses logika setelah tampilan dimuat
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tentukan berapa XP yang didapat saat menyelesaikan kuis ini
        // Misalnya kita set mendapat 50 XP
        val xpDidapat = 50

        // Panggil helper untuk menambahkan XP ke database Firebase
        AktivitasHelper.tambahPoint(requireContext(), xpDidapat)
    }
}