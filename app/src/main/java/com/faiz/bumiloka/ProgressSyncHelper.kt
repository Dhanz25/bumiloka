package com.faiz.bumiloka

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object ProgressSyncHelper {

    fun syncProgressFromFirebase(context: Context, onComplete: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return onComplete()
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        // Ambil data progress dari Firebase
        db.child("progress_sync").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val prefsKuis1 = context.getSharedPreferences("KUIS_${userId}_LEVEL_1", Context.MODE_PRIVATE).edit()
                val prefsKuis2 = context.getSharedPreferences("KUIS_${userId}_LEVEL_2", Context.MODE_PRIVATE).edit()
                val prefsKuis3 = context.getSharedPreferences("KUIS_${userId}_LEVEL_3", Context.MODE_PRIVATE).edit()
                val prefsMisi1 = context.getSharedPreferences("MISI_${userId}_LEVEL_1", Context.MODE_PRIVATE).edit()
                val prefsMisi2 = context.getSharedPreferences("MISI_${userId}_LEVEL_2", Context.MODE_PRIVATE).edit()
                val prefsMisi3 = context.getSharedPreferences("MISI_${userId}_LEVEL_3", Context.MODE_PRIVATE).edit()
                val prefsTantangan = context.getSharedPreferences("TANTANGAN_STATUS_$userId", Context.MODE_PRIVATE).edit()

                snapshot.children.forEach { child ->
                    val key = child.key ?: ""
                    val value = child.value
                    
                    when {
                        key.startsWith("KUIS_1_") -> if (value is Boolean) prefsKuis1.putBoolean(key.replace("KUIS_1_", ""), value) else if (value is Long) prefsKuis1.putInt(key.replace("KUIS_1_", ""), value.toInt())
                        key.startsWith("KUIS_2_") -> if (value is Boolean) prefsKuis2.putBoolean(key.replace("KUIS_2_", ""), value) else if (value is Long) prefsKuis2.putInt(key.replace("KUIS_2_", ""), value.toInt())
                        key.startsWith("KUIS_3_") -> if (value is Boolean) prefsKuis3.putBoolean(key.replace("KUIS_3_", ""), value) else if (value is Long) prefsKuis3.putInt(key.replace("KUIS_3_", ""), value.toInt())
                        key.startsWith("MISI_1_") -> if (value is Boolean) prefsMisi1.putBoolean(key.replace("MISI_1_", ""), value)
                        key.startsWith("MISI_2_") -> if (value is Boolean) prefsMisi2.putBoolean(key.replace("MISI_2_", ""), value)
                        key.startsWith("MISI_3_") -> if (value is Boolean) prefsMisi3.putBoolean(key.replace("MISI_3_", ""), value)
                        key.startsWith("CHALLENGE_") -> if (value is Boolean) prefsTantangan.putBoolean(key.replace("CHALLENGE_", ""), value)
                    }
                }
                
                prefsKuis1.apply(); prefsKuis2.apply(); prefsKuis3.apply()
                prefsMisi1.apply(); prefsMisi2.apply(); prefsMisi3.apply()
                prefsTantangan.apply()
                
                Log.d("SyncHelper", "Progress berhasil dipulihkan dari Firebase")
            }
            onComplete()
        }.addOnFailureListener {
            Log.e("SyncHelper", "Gagal sinkronisasi: ${it.message}")
            onComplete()
        }
    }

    fun saveProgressToFirebase(userId: String, category: String, key: String, value: Any) {
        val db = FirebaseDatabase.getInstance().reference
            .child("users").child(userId).child("progress_sync")
        
        db.child("${category}_${key}").setValue(value)
    }
}
