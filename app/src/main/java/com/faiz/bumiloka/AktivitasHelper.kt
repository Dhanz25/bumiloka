package com.faiz.bumiloka

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.faiz.bumiloka.data.local.NotificationDatabase
import com.faiz.bumiloka.data.model.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AktivitasHelper {

    private fun safeToInt(value: Any?): Int {
        return when (value) {
            is Long -> value.toInt()
            is Int -> value
            is Double -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        }
    }

    private fun saveLocalNotification(context: Context, title: String, body: String, category: String = "Sistem") {
        val database = NotificationDatabase.getDatabase(context.applicationContext)
        val dao = database.notificationDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.insertNotification(
                NotificationEntity(
                    title = title,
                    body = body,
                    timestamp = System.currentTimeMillis(),
                    category = category
                )
            )
        }
    }

    fun tambahPoint(context: Context?, tambahanXP: Int, sumber: String = "Aktivitas", showNotification: Boolean = true) {
        val ctx = context ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        db.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) return@addOnSuccessListener
            
            try {
                var currentLevel = safeToInt(snapshot.child("level").value).let { if (it <= 0) 1 else it }
                var currentXP = safeToInt(snapshot.child("xp").value)
                var totalPoint = safeToInt(snapshot.child("totalPoint").value)

                Log.d("AktivitasHelper", "XP Awal: $currentXP, Tambahan: $tambahanXP dari $sumber")

                currentXP += tambahanXP
                totalPoint += tambahanXP

                var naikLevel = false
                while (currentXP >= 100) {
                    currentXP -= 100
                    currentLevel++
                    naikLevel = true
                }

                Log.d("AktivitasHelper", "XP Akhir: $currentXP, Level Baru: $currentLevel, Naik Level: $naikLevel")

                val updates = mapOf(
                    "level" to currentLevel,
                    "xp" to currentXP,
                    "totalPoint" to totalPoint
                )

                db.updateChildren(updates).addOnSuccessListener {
                    // ✅ Catat aktivitas ke riwayat aktivitas user
                    AktivitasManager.tambahAktivitas(ctx, "Mendapatkan +$tambahanXP XP dari $sumber", "XP", tambahanXP)

                    // Tampilkan Toast agar user tahu berapa XP yang didapat
                    Handler(Looper.getMainLooper()).post {
                        if (!naikLevel) {
                            Toast.makeText(ctx, "+$tambahanXP XP ($currentXP/100)", Toast.LENGTH_SHORT).show()
                        }
                    }

                    if (showNotification) {
                        val msg = if (naikLevel) "Luar biasa! Kamu mencapai Level $currentLevel!" 
                                 else "Selamat! Kamu mendapatkan +$tambahanXP XP dari $sumber."
                        saveLocalNotification(ctx, if (naikLevel) "Naik Level!" else "Poin Didapat!", msg, "Reward")
                    }
                }

                if (naikLevel) {
                    if (ctx is Activity && !ctx.isFinishing) {
                        showLevelUpPopup(ctx, userId, currentLevel, currentXP)
                    }
                }
            } catch (e: Exception) { Log.e("AktivitasHelper", "Error: ${e.message}") }
        }
    }

    private fun showLevelUpPopup(context: Context, userId: String, nextLevel: Int, sisaXP: Int) {
        try {
            val inflater = LayoutInflater.from(context)
            val notifView = inflater.inflate(R.layout.popup_naiklevel, null)
            val notifDialog = AlertDialog.Builder(context)
                .setView(notifView)
                .setCancelable(false)
                .create()

            notifDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            notifDialog.show()

            notifView.findViewById<Button>(R.id.btnLanjutPopup)?.setOnClickListener {
                notifDialog.dismiss()
                LevelHelper.resetProgressPerLevel(context, userId, nextLevel, sisaXP) {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) { Log.e("AktivitasHelper", "Dialog Error: ${e.message}") }
    }

    fun tambahMisiSelesai(context: Context?, sumber: String = "Misi", showNotification: Boolean = true) {
        val ctx = context ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        db.child("misiTercapai").get().addOnSuccessListener { snapshot ->
            val currentMisi = safeToInt(snapshot.value)
            db.child("misiTercapai").setValue(currentMisi + 1).addOnSuccessListener {
                // ✅ Catat aktivitas misi selesai
                AktivitasManager.tambahAktivitas(ctx, "Menyelesaikan $sumber", "MISI", 0)
                
                if (showNotification) {
                    saveLocalNotification(ctx, "Misi Selesai", "$sumber telah kamu selesaikan.", "Aktivitas")
                }
            }
        }
    }
}
