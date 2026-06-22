package com.faiz.bumiloka

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object AppResetHelper {

    fun resetSemuaData(context: Context, onComplete: (Boolean) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return

        // 1. Reset Firebase Realtime Database
        val dbRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        val updates = hashMapOf<String, Any?>(
            "totalPoint" to 0,
            "xp" to 0,
            "level" to 1,
            "highestUnlockedLevel" to 1,
            "misiTercapai" to 0,
            "totalLencana" to 0,
            "lencana_dimiliki" to null,
            "tantangan_bonus_selesai" to null
        )

        dbRef.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 2. Reset SharedPreferences
                resetSharedPreferences(context, userId)
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    private fun resetSharedPreferences(context: Context, userId: String) {
        val prefsToReset = listOf(
            "LEVEL_SYSTEM",
            "BADGE_PREF",
            "MASTER_KUIS_BADGE",
            "KUIS_${userId}_LEVEL_1",
            "KUIS_${userId}_LEVEL_2",
            "KUIS_${userId}_LEVEL_3",
            "TANTANGAN_PENJELAJAH_${userId}",
            "tantangan_pref",
            "MISI_${userId}_LEVEL_1",
            "MISI_${userId}_LEVEL_2",
            "MISI_${userId}_LEVEL_3",
            "AKTIVITAS_PREF",
            "BumilokaPrefs",
            "KUIS_$userId",
            "APP",
            "BONUS_CHALLENGES_$userId"
        )

        prefsToReset.forEach { prefName ->
            context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().clear().apply()
        }

        // Kembalikan level sistem ke awal
        context.getSharedPreferences("LEVEL_SYSTEM", Context.MODE_PRIVATE)
            .edit()
            .putInt("current_level", 1)
            .apply()
    }
}
