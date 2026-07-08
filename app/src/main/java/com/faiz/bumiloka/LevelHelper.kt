package com.faiz.bumiloka

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object LevelHelper {

    /**
     * Mengambil level aktif yang sedang dipilih oleh user (untuk filter konten).
     * Tidak lagi melakukan sinkronisasi otomatis dari Firebase agar tidak mengganggu flow user.
     */
    fun getCurrentLevel(context: Context, callback: (Int) -> Unit) {
        val localLevel = getCurrentLevelLocal(context)
        callback(localLevel)
        
        // Sinkronisasi dari Firebase hanya dilakukan saat inisialisasi awal atau via sync manual
        // Untuk sekarang, kita kembalikan saja localLevel agar filter tidak berubah tiba-tiba.
    }

    fun getHighestUnlockedLevel(callback: (Int) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return callback(1)
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        
        db.child("highestUnlockedLevel").get().addOnSuccessListener { snapshot ->
            val level = (snapshot.value as? Long)?.toInt() ?: 1
            callback(if (level > 3) 3 else level)
        }.addOnFailureListener {
            callback(1)
        }
    }

    fun getCurrentLevelLocal(context: Context): Int {
        val pref = context.getSharedPreferences("LEVEL_SYSTEM", Context.MODE_PRIVATE)
        return pref.getInt("current_level", 1)
    }

    /**
     * Menyimpan level yang dipilih secara eksplisit oleh user.
     */
    fun saveSelectedLevel(context: Context, level: Int, onComplete: () -> Unit = {}) {
        val targetLevel = if (level > 3) 3 else level
        val pref = context.getSharedPreferences("LEVEL_SYSTEM", Context.MODE_PRIVATE)
        pref.edit().putInt("current_level", targetLevel).apply()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        db.child("level").setValue(targetLevel).addOnSuccessListener { onComplete() }
    }

    fun resetProgressPerLevel(context: Context, userId: String, nextLevel: Int, sisaXP: Int, onComplete: () -> Unit) {
        val safeNextLevel = if (nextLevel > 3) 3 else nextLevel
        val prefSystem = context.getSharedPreferences("LEVEL_SYSTEM", Context.MODE_PRIVATE)
        prefSystem.edit().putInt("current_level", safeNextLevel).apply()

        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        val updates = mutableMapOf<String, Any>(
            "level" to safeNextLevel,
            "xp" to sisaXP,
            "misiTercapai" to 0,
            "highestUnlockedLevel" to safeNextLevel
        )
        db.updateChildren(updates).addOnSuccessListener { onComplete() }
    }

    fun openQuizFragment(level: Int, index: Int, fragmentManager: FragmentManager) {
        val quizId = when(level) {
            1 -> index.toString()
            2 -> (index + 3).toString()
            3 -> (index + 6).toString()
            else -> "1"
        }
        val fragment = QuizSoalFragment.newInstance(quizId, level)
        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
