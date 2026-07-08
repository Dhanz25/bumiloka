package com.faiz.bumiloka

import android.content.Context
import android.util.Log
import com.faiz.bumiloka.model.Tantangan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object TantanganStatusHelper {

    fun isTantanganSelesai(context: Context, challengeId: String): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val prefs = context.getSharedPreferences("TANTANGAN_STATUS_$userId", Context.MODE_PRIVATE)
        return prefs.getBoolean("challenge_${challengeId}_selesai", false)
    }

    fun syncAllProgress(context: Context?, level: Int, type: String, id: String, skor: Int = 0) {
        val ctx = context ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val kuisPref = ctx.getSharedPreferences("KUIS_${userId}_LEVEL_$level", Context.MODE_PRIVATE)
        val misiPref = ctx.getSharedPreferences("MISI_${userId}_LEVEL_$level", Context.MODE_PRIVATE)
        val tantanganPref = ctx.getSharedPreferences("TANTANGAN_STATUS_$userId", Context.MODE_PRIVATE)

        val cleanId = id.replace("kuis_", "").trim()

        when (type) {
            "MATERI" -> {
                misiPref.edit().putBoolean("misi1_selesai", true).apply()
                tantanganPref.edit().putBoolean("materi_${cleanId}_selesai", true).apply()
                if (userId != "guest") {
                    ProgressSyncHelper.saveProgressToFirebase(userId, "MISI_$level", "misi1_selesai", true)
                    ProgressSyncHelper.saveProgressToFirebase(userId, "CHALLENGE", "materi_${cleanId}_selesai", true)
                }
            }
            "QUIZ" -> {
                kuisPref.edit().apply {
                    putBoolean("kuis_${id}_selesai", true)
                    putInt("kuis_${id}_skor", skor)
                    putBoolean("kuis_${cleanId}_selesai", true)
                    putInt("kuis_${cleanId}_skor", skor)
                    
                    val idInt = cleanId.toIntOrNull() ?: 1
                    val quizIndex = if (idInt % 3 == 0) 3 else idInt % 3
                    putBoolean("quiz${quizIndex}_selesai", true)
                    putInt("quiz${quizIndex}_nilai", skor)
                    
                    if (quizIndex == 1) putBoolean("materi1_selesai", true)
                    if (quizIndex == 2) putBoolean("quiz2_selesai", true)
                    if (quizIndex == 3) putBoolean("quiz3_selesai", true)
                }.commit()

                if (skor > 0) misiPref.edit().putBoolean("misi2_selesai", true).apply()
                if (skor >= 75) {
                    misiPref.edit().putBoolean("misi3_selesai", true).apply()
                }

                tantanganPref.edit().apply {
                    putBoolean("quiz_${cleanId}_selesai", true)
                    putInt("quiz_${cleanId}_nilai", skor)
                }.apply()

                if (userId != "guest") {
                    ProgressSyncHelper.saveProgressToFirebase(userId, "KUIS_$level", "kuis_${cleanId}_selesai", true)
                    ProgressSyncHelper.saveProgressToFirebase(userId, "KUIS_$level", "kuis_${cleanId}_skor", skor)
                    ProgressSyncHelper.saveProgressToFirebase(userId, "MISI_$level", "misi2_selesai", true)
                    if (skor >= 75) ProgressSyncHelper.saveProgressToFirebase(userId, "MISI_$level", "misi3_selesai", true)
                }
            }
        }
        checkAutoCompletedChallenges(ctx, level)
    }

    private fun checkAutoCompletedChallenges(context: Context, level: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference
        val tantanganPref = context.getSharedPreferences("TANTANGAN_STATUS_$userId", Context.MODE_PRIVATE)

        db.child("tantangan").get().addOnSuccessListener { snapshot ->
            for (child in snapshot.children) {
                try {
                    val map = child.value as? Map<*, *> ?: continue
                    val tId = child.key ?: ""
                    val tLevel = (map["level"] as? Long)?.toInt() ?: 1
                    if (tLevel != level || isTantanganSelesai(context, tId)) continue

                    val tType = map["type"]?.toString() ?: "SINGLE"
                    val tMateriId = map["materiId"]?.toString() ?: ""
                    val tQuizId = map["quizId"]?.toString() ?: ""
                    val tBadgeId = map["badgeId"]?.toString() ?: ""

                    if (tType == "SINGLE") {
                        val matDone = tMateriId.isEmpty() || tantanganPref.getBoolean("materi_${tMateriId}_selesai", false)
                        val quizDone = tQuizId.isEmpty() || tantanganPref.getBoolean("quiz_${tQuizId}_selesai", false)
                        
                        // Jika tantangan selesai (Materi & Kuis beres)
                        if (matDone && quizDone) {
                            setTantanganSelesai(context, tId, tMateriId, tQuizId, 100)
                            // OTOMATIS BERIKAN LENCANA JIKA ADA
                            if (tBadgeId.isNotEmpty()) {
                                BadgeHelper.tambahBadge(context, tBadgeId, true)
                            }
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    fun setTantanganSelesai(context: Context?, challengeId: String, materiId: String, quizId: String, skor: Int) {
        val ctx = context ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val prefs = ctx.getSharedPreferences("TANTANGAN_STATUS_$userId", Context.MODE_PRIVATE)
        val key = "challenge_${challengeId}_selesai"
        
        if (prefs.getBoolean(key, false)) return
        prefs.edit().putBoolean(key, true).apply()
        
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        db.child("tantangan_selesai").child(challengeId).setValue(true)
        ProgressSyncHelper.saveProgressToFirebase(userId, "CHALLENGE", key, true)
    }
}
