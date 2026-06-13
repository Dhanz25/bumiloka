package com.faiz.bumiloka

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object TantanganStatusHelper {

    fun isMasterKuisSelesai(context: Context): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val pref = context.getSharedPreferences("KUIS_${userId}_LEVEL_1", Context.MODE_PRIVATE)

        val quiz1 = pref.getBoolean("materi1_selesai", false)
        val quiz2 = pref.getBoolean("quiz2_selesai", false)
        val quiz3 = pref.getBoolean("quiz3_selesai", false)

        val nilai1 = pref.getInt("nilai_materi1", 0)
        val nilai2 = pref.getInt("quiz2_nilai", 0)
        val nilai3 = pref.getInt("quiz3_nilai", 0)

        return quiz1 && quiz2 && quiz3 && nilai1 >= 75 && nilai2 >= 75 && nilai3 >= 75
    }

    fun isPenjelajahSelesai(context: Context): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        return context.getSharedPreferences("TANTANGAN_PENJELAJAH_$userId", Context.MODE_PRIVATE)
            .getBoolean("tantangan_selesai", false)
    }

    fun isTantanganBonusSelesai(context: Context, materiId: String, quizId: String): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        
        // Cek progress materi dari SharedPreferences MISI
        // Menggunakan "LEVEL_1" sesuai standar yang ada di project saat ini
        val prefMisi = context.getSharedPreferences("MISI_${userId}_LEVEL_1", Context.MODE_PRIVATE)
        val materiSelesai = prefMisi.getBoolean("misi${materiId}_selesai", false)

        // Cek progress quiz dari SharedPreferences KUIS
        val prefKuis = context.getSharedPreferences("KUIS_${userId}_LEVEL_1", Context.MODE_PRIVATE)
        val quizSelesai = prefKuis.getBoolean("quiz${quizId}_selesai", false)
        val nilaiQuiz = prefKuis.getInt("quiz${quizId}_nilai", 0)

        // Tantangan selesai jika materi OK dan kuis OK (nilai >= 75)
        return materiSelesai && quizSelesai && nilaiQuiz >= 75
    }
}