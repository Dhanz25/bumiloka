package com.faiz.bumiloka

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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

    fun isTantanganSelesai(context: Context, challengeId: String): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val prefs = context.getSharedPreferences("TANTANGAN_STATUS_$userId", Context.MODE_PRIVATE)
        return prefs.getBoolean("challenge_${challengeId}_selesai", false)
    }

    // Legacy support for Int IDs if needed, but updated to handle String IDs primarily
    fun isTantanganBonusSelesai(context: Context, materiId: String, quizId: String): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val bonusPrefs = context.getSharedPreferences("BONUS_CHALLENGES_$userId", Context.MODE_PRIVATE)
        
        val materiSelesai = bonusPrefs.getBoolean("materi_${materiId}_selesai", false)
        val quizSelesai = bonusPrefs.getBoolean("quiz_${quizId}_selesai", false)
        val nilaiQuiz = bonusPrefs.getInt("quiz_${quizId}_nilai", 0)

        return (materiSelesai && quizSelesai && nilaiQuiz >= 75) || isTantanganSelesai(context, materiId)
    }

    fun setTantanganSelesai(context: Context, challengeId: String, materiId: String, quizId: String, skor: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        // Simpan Lokal
        val prefs = context.getSharedPreferences("TANTANGAN_STATUS_$userId", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("challenge_${challengeId}_selesai", true)
            .putBoolean("materi_${materiId}_selesai", true)
            .putBoolean("quiz_${quizId}_selesai", true)
            .putInt("quiz_${quizId}_nilai", skor)
            .apply()

        // Simpan Firebase
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        db.child("tantangan_selesai").child(challengeId).setValue(true)
    }
}