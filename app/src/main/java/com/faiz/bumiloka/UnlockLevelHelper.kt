package com.faiz.bumiloka

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object UnlockLevelHelper {

    fun checkAndUnlockNextLevel(context: Context, currentLevel: Int) {
        if (currentLevel >= 3) return

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val prefMisi = context.getSharedPreferences("MISI_${userId}_LEVEL_$currentLevel", Context.MODE_PRIVATE)
        
        val misi1 = prefMisi.getBoolean("misi1_selesai", false)
        val misi2 = prefMisi.getBoolean("misi2_selesai", false)
        val misi3 = prefMisi.getBoolean("misi3_selesai", false)

        if (misi1 && misi2 && misi3) {
            unlockNextLevel(context, currentLevel + 1)
        }
    }

    private fun unlockNextLevel(context: Context, nextLevel: Int) {
        if (nextLevel > 3) return

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        db.child("highestUnlockedLevel").get().addOnSuccessListener { snapshot ->
            val highest = (snapshot.value as? Long)?.toInt() ?: 1

            if (nextLevel > highest) {
                // Hanya update level yang terbuka, jangan paksa ganti level aktif
                db.child("highestUnlockedLevel").setValue(nextLevel)
                
                // Hapus baris ini agar user tidak tiba-tiba pindah level saat masih di fragment sebelumnya
                // db.child("level").setValue(nextLevel)
                // val pref = context.getSharedPreferences("LEVEL_SYSTEM", Context.MODE_PRIVATE)
                // pref.edit().putInt("current_level", nextLevel).apply()
                
                Log.d("UnlockLevel", "Level $nextLevel berhasil dibuka!")
            }
        }.addOnFailureListener { e ->
            Log.e("UnlockLevel", "Gagal unlock level: ${e.message}")
        }
    }

    fun isLevelUnlocked(level: Int, highestUnlocked: Int): Boolean {
        if (level > 3) return false
        return level <= highestUnlocked
    }
}
