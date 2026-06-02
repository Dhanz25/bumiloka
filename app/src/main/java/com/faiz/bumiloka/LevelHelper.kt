package com.faiz.bumiloka

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object LevelHelper {

    /**
     * Mengambil level yang SEDANG AKTIF/DILIHAT user.
     */
    fun getCurrentLevel(callback: (Int) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return callback(1)
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        
        db.child("level").get().addOnSuccessListener { snapshot ->
            val level = snapshot.getValue(Int::class.java) ?: 1
            callback(level)
        }.addOnFailureListener {
            callback(1)
        }
    }

    /**
     * Mengambil level TERTINGGI yang sudah terbuka oleh user.
     */
    fun getHighestUnlockedLevel(callback: (Int) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return callback(1)
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        
        db.child("highestUnlockedLevel").get().addOnSuccessListener { snapshot ->
            val level = snapshot.getValue(Int::class.java) ?: 1
            callback(level)
        }.addOnFailureListener {
            callback(1)
        }
    }

    /**
     * Menyimpan level yang sedang dipilih user ke Firebase.
     */
    fun saveSelectedLevel(context: Context, level: Int, onComplete: () -> Unit = {}) {

        // ✅ simpan local dulu (biar instant)
        val pref = context.getSharedPreferences("LEVEL_SYSTEM", Context.MODE_PRIVATE)

        pref.edit()
            .putInt("current_level", level)
            .apply()

        // ✅ sync firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        db.child("level")
            .setValue(level)
            .addOnSuccessListener {
                onComplete()
            }
    }

    /**
     * Mendapatkan judul Quiz berdasarkan level dan indeks kartu.
     */
    fun getQuizTitle(level: Int, index: Int): String {
        return when (level) {
            1 -> when (index) {
                1 -> "Quiz Peduli Lingkungan"
                2 -> "Quiz Kelola Sampah"
                3 -> "Quiz Hemat Air"
                else -> "Quiz Lingkungan"
            }
            2 -> when (index) {
                1 -> "Quiz Energi Terbarukan"
                2 -> "Quiz Pemanasan Global"
                3 -> "Quiz Ekosistem Laut"
                else -> "Quiz Level 2"
            }
            else -> "Quiz Level $level - $index"
        }
    }

    /**
     * Mendapatkan gambar Quiz berdasarkan level dan indeks.
     */
    fun getQuizImage(level: Int, index: Int): Int {
        return when (level) {
            1 -> when (index) {
                1 -> R.drawable.img_lingkungan
                2 -> R.drawable.img_sampah
                3 -> R.drawable.img_air
                else -> R.drawable.img_lingkungan
            }
            else -> R.drawable.img
        }
    }

    /**
     * Navigasi ke Fragment Quiz yang sesuai berdasarkan level.
     */
    fun openQuizFragment(level: Int, index: Int, fragmentManager: FragmentManager) {
        val fragment: Fragment = when (level) {
            1 -> when (index) {
                1 -> QuizSoalFragment.newInstance(1)
                2 -> QuizSoal2Fragment.newInstance(2)
                3 -> QuizSoal3Fragment.newInstance(3)
                else -> QuizSoalFragment.newInstance(1)
            }
            else -> QuizSoalFragment.newInstance(index + (level - 1) * 3) 
        }

        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Reset Progress User (Lokal SharedPreferences & Firebase)
     * Digunakan saat naik level otomatis atau reset manual.
     */
    fun resetProgressPerLevel(context: Context, userId: String, nextLevel: Int, sisaXP: Int, onComplete: () -> Unit) {
        val prefKuis = context.getSharedPreferences("KUIS_$userId", Context.MODE_PRIVATE)
        val prefMisi = context.getSharedPreferences("MISI_$userId", Context.MODE_PRIVATE)

        prefKuis.edit().clear().apply()
        prefMisi.edit().clear().apply()

        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        val updates = mutableMapOf<String, Any>(
            "level" to nextLevel,
            "xp" to sisaXP,
            "misiTercapai" to 0,
            "highestUnlockedLevel" to nextLevel
        )

        db.updateChildren(updates).addOnSuccessListener {
            onComplete()
        }
    }
    fun getCurrentLevelLocal(context: Context): Int {
        val pref = context.getSharedPreferences("LEVEL_SYSTEM", Context.MODE_PRIVATE)
        return pref.getInt("current_level", 1)
    }
}
