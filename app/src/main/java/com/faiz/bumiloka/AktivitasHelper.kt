package com.faiz.bumiloka

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import android.widget.Button

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

    fun tambahPoint(context: Context?, tambahanXP: Int) {
        val ctx = context ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        db.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                var currentLevel = safeToInt(snapshot.child("level").value).let { if (it <= 0) 1 else it }
                var currentXP = safeToInt(snapshot.child("xp").value)
                var totalPoint = safeToInt(snapshot.child("totalPoint").value)

                currentXP += tambahanXP
                totalPoint += tambahanXP

                var naikLevel = false
                while (currentXP >= 100) {
                    currentXP -= 100
                    currentLevel++
                    naikLevel = true
                }

                val updates = mapOf(
                    "level" to currentLevel,
                    "xp" to currentXP,
                    "totalPoint" to totalPoint
                )

                db.updateChildren(updates)

                if (naikLevel) {
                    showLevelUpPopup(ctx, userId, currentLevel, currentXP)
                }
            }
        }
    }

    private fun showLevelUpPopup(context: Context, userId: String, nextLevel: Int, sisaXP: Int) {
        val notifView = LayoutInflater.from(context).inflate(R.layout.popup_naiklevel, null)
        val notifDialog = AlertDialog.Builder(context)
            .setView(notifView)
            .setCancelable(false)
            .create()

        notifDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        notifDialog.show()
        notifDialog.window?.setGravity(Gravity.CENTER)

        val btnLanjut = notifView.findViewById<Button>(R.id.btnLanjutPopup)
        btnLanjut?.setOnClickListener {
            notifDialog.dismiss()
            LevelHelper.resetProgressPerLevel(context, userId, nextLevel, sisaXP) {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
        }
    }

    fun tambahMisiSelesai() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        db.child("misiTercapai").get().addOnSuccessListener { snapshot ->
            val currentMisi = safeToInt(snapshot.value)
            db.child("misiTercapai").setValue(currentMisi + 1)
        }
    }

    fun tambahLencana(idLencana: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        // Prevent duplicate badge logic
        db.child("lencana_dimiliki").child(idLencana).get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                db.child("lencana_dimiliki").child(idLencana).setValue(true)
                db.child("totalLencana").get().addOnSuccessListener { totalSnap ->
                    val currentTotal = safeToInt(totalSnap.value)
                    db.child("totalLencana").setValue(currentTotal + 1)
                }
            }
        }
    }
}