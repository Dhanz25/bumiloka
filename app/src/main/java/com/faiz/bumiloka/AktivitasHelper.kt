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

    fun tambahPoint(context: Context, tambahanXP: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        db.get().addOnSuccessListener { snapshot ->
            var currentLevel = snapshot.child("level").getValue(Int::class.java) ?: 1
            var currentXP = snapshot.child("xp").getValue(Int::class.java) ?: 0
            var totalPoint = snapshot.child("totalPoint").getValue(Int::class.java) ?: 0

            val targetXP = 100
            currentXP += tambahanXP
            totalPoint += tambahanXP

            var naikLevel = false
            while (currentXP >= targetXP) {
                currentXP -= targetXP
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
                showLevelUpPopup(context, userId, currentLevel, currentXP)
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
        btnLanjut.setOnClickListener {
            notifDialog.dismiss()
            
            // Gunakan LevelHelper untuk reset progress
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
            val currentMisi = snapshot.getValue(Int::class.java) ?: 0
            db.child("misiTercapai").setValue(currentMisi + 1)
        }
    }

    fun tambahLencana(idLencana: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        db.child("lencana_dimiliki").child(idLencana).setValue(true)
        db.child("totalLencana").get().addOnSuccessListener { snapshot ->
            val currentTotal = snapshot.getValue(Int::class.java) ?: 0
            db.child("totalLencana").setValue(currentTotal + 1)
        }
    }
}
