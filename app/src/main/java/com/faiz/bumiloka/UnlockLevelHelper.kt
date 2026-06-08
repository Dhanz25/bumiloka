package com.faiz.bumiloka

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object UnlockLevelHelper {

    /**
     * Mengecek apakah semua misi di level tertentu sudah selesai.
     * Logika ini bisa disesuaikan dengan kebutuhan (misal: cek SharedPreferences atau Firebase)
     */
    fun checkAndUnlockNextLevel(context: Context, currentLevel: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val prefMisi = context.getSharedPreferences(
            "MISI_${userId}_LEVEL_$currentLevel",
            Context.MODE_PRIVATE
        )
        
        // Misal syarat naik ke level berikutnya adalah 3 misi utama selesai
        val misi1 = prefMisi.getBoolean("misi1_selesai", false)
        val misi2 = prefMisi.getBoolean("misi2_selesai", false)
        val misi3 = prefMisi.getBoolean("misi3_selesai", false)

        if (misi1 && misi2 && misi3) {
            unlockNextLevel(context, currentLevel + 1)
        }
    }

    private fun unlockNextLevel(context: Context, nextLevel: Int) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val db = FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(userId)

        db.child("highestUnlockedLevel")
            .get()
            .addOnSuccessListener { snapshot ->

                val highest = snapshot.getValue(Int::class.java) ?: 1

                if (nextLevel > highest) {

                    // ✅ update firebase
                    db.child("highestUnlockedLevel").setValue(nextLevel)

                    // ✅ pindah level aktif
                    db.child("level").setValue(nextLevel)

                    // ✅ simpan local instant
                    val pref = context.getSharedPreferences(
                        "LEVEL_SYSTEM",
                        Context.MODE_PRIVATE
                    )

                    pref.edit()
                        .putInt("current_level", nextLevel)
                        .apply()
                }
            }
    }

    fun isLevelUnlocked(level: Int, highestUnlocked: Int): Boolean {
        return level <= highestUnlocked
    }
}
