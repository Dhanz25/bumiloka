package com.faiz.bumiloka

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object LevelHelper {

    fun getCurrentLevel(context: Context, callback: (Int) -> Unit) {
        val localLevel = getCurrentLevelLocal(context)
        callback(localLevel)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val db = FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(userId)

        db.child("level")
            .get()
            .addOnSuccessListener { snapshot ->
                var firebaseLevel = snapshot.getValue(Int::class.java) ?: 1
                if (firebaseLevel > 3) firebaseLevel = 3

                if (firebaseLevel != localLevel) {
                    val pref = context.getSharedPreferences("LEVEL_SYSTEM", Context.MODE_PRIVATE)
                    pref.edit().putInt("current_level", firebaseLevel).apply()
                    callback(firebaseLevel)
                }
            }
    }

    fun getHighestUnlockedLevel(callback: (Int) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return callback(1)
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        
        db.child("highestUnlockedLevel").get().addOnSuccessListener { snapshot ->
            var level = snapshot.getValue(Int::class.java) ?: 1
            if (level > 3) level = 3
            callback(level)
        }.addOnFailureListener {
            callback(1)
        }
    }

    fun saveSelectedLevel(context: Context, level: Int, onComplete: () -> Unit = {}) {
        val targetLevel = if (level > 3) 3 else level
        val pref = context.getSharedPreferences("LEVEL_SYSTEM", Context.MODE_PRIVATE)
        pref.edit().putInt("current_level", targetLevel).apply()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        db.child("level").setValue(targetLevel).addOnSuccessListener { onComplete() }
    }

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
            3 -> when (index) {
                1 -> "Quiz Biodiversity"
                2 -> "Quiz Perubahan Iklim"
                3 -> "Quiz Hutan Hujan"
                else -> "Quiz Level 3"
            }
            else -> "Quiz Level $level - $index"
        }
    }

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

    fun openQuizFragment(level: Int, index: Int, fragmentManager: FragmentManager) {
        val fragment: Fragment = when (level) {
            1 -> when (index) {
                1 -> QuizSoalFragment.newInstance("1")
                2 -> QuizSoal2Fragment.newInstance(2)
                3 -> QuizSoal3Fragment.newInstance(3)
                else -> QuizSoalFragment.newInstance("1")
            }
            2 -> when (index) {
                1 -> QuizSoalFragment.newInstance("4")
                2 -> QuizSoal2Fragment.newInstance(5)
                3 -> QuizSoal3Fragment.newInstance(6)
                else -> QuizSoalFragment.newInstance("4")
            }
            3 -> when (index) {
                1 -> QuizSoalFragment.newInstance("7")
                2 -> QuizSoal2Fragment.newInstance(8)
                3 -> QuizSoal3Fragment.newInstance(9)
                else -> QuizSoalFragment.newInstance("7")
            }
            else -> QuizSoalFragment.newInstance("1")
        }

        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
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

    fun getCurrentLevelLocal(context: Context): Int {
        val pref = context.getSharedPreferences("LEVEL_SYSTEM", Context.MODE_PRIVATE)
        return pref.getInt("current_level", 1)
    }
}
