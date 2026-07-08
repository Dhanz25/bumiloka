package com.faiz.bumiloka

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.faiz.bumiloka.model.Badge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object BadgeHelper {

    private const val PREF_NAME = "BADGE_PREF"

    fun tambahBadge(context: Context, badgeId: String, showPopup: Boolean = false) {
        if (badgeId.isEmpty()) return
        
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Jika sudah punya secara lokal, tetap update cloud untuk jaga-jaga
        pref.edit().putBoolean(badgeId, true).apply()

        val db = FirebaseDatabase.getInstance().reference
        db.child("users").child(userId).child("badges_earned").child(badgeId).setValue(true)
        
        db.child("users").child(userId).child("totalLencana").get().addOnSuccessListener {
            val currentTotal = (it.value as? Long)?.toInt() ?: 0
            db.child("users").child(userId).child("totalLencana").setValue(currentTotal + 1)
        }

        if (showPopup && !pref.getBoolean("${badgeId}_popup_shown", false)) {
            fetchAndShowBadgePopup(context, badgeId)
            pref.edit().putBoolean("${badgeId}_popup_shown", true).apply()
        }
    }

    private fun fetchAndShowBadgePopup(context: Context, badgeId: String) {
        FirebaseDatabase.getInstance().reference.child("badges").child(badgeId).get()
            .addOnSuccessListener { snapshot ->
                val badge = snapshot.getValue(Badge::class.java)
                if (badge != null) {
                    badge.id = snapshot.key ?: ""
                    showBadgePopup(context, badge)
                }
            }
    }

    private fun showBadgePopup(context: Context, badge: Badge) {
        try {
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.popup_badge_earned, null)
            val dialog = AlertDialog.Builder(context).setView(dialogView).create()
            
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val ivBadge = dialogView.findViewById<ImageView>(R.id.ivBadgeLarge)
            val tvTitle = dialogView.findViewById<TextView>(R.id.tvBadgeTitle)
            val btnKlaim = dialogView.findViewById<Button>(R.id.btnKlaim)

            tvTitle.text = badge.nama
            BadgeVisualHelper.renderBadge(ivBadge, badge.nama, badge.level)

            btnKlaim.setOnClickListener { dialog.dismiss() }
            dialog.show()
        } catch (e: Exception) {
            Log.e("BadgeHelper", "Error popup: ${e.message}")
        }
    }

    fun getTotalBadge(context: Context): Int {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.all.count { it.value == true && !it.key.endsWith("_popup_shown") }
    }

    fun punyaBadge(context: Context, badgeId: String): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(badgeId, false)
    }

    fun syncBadges(context: Context, onComplete: ((Int) -> Unit)? = null) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            onComplete?.invoke(0)
            return
        }
        
        FirebaseDatabase.getInstance().reference.child("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
                // Jangan clear total, hanya sync ID yang ada
                val ownedIds = mutableSetOf<String>()
                
                snapshot.child("badges_earned").children.forEach { child ->
                    child.key?.let { id -> 
                        pref.putBoolean(id, true)
                        ownedIds.add(id)
                    }
                }
                snapshot.child("lencana").children.forEach { child ->
                    child.key?.let { id -> 
                        pref.putBoolean(id, true)
                        ownedIds.add(id)
                    }
                }
                pref.apply()
                onComplete?.invoke(ownedIds.size)
            }.addOnFailureListener {
                onComplete?.invoke(getTotalBadge(context))
            }
    }
}
