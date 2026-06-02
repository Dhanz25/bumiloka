package com.faiz.bumiloka

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object ProfileHelper {
    fun checkProfileOnce(fragment: Fragment) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userId = user.uid
        val db = FirebaseDatabase.getInstance().reference

        db.child("users").child(userId).get().addOnSuccessListener { snapshot ->
            val isComplete = snapshot.child("isProfileComplete")
                .getValue(Boolean::class.java) ?: false

            if (!isComplete) {
                showPopup(fragment)
            }
        }
    }

    fun requireProfile(fragment: Fragment, action: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userId = user.uid
        val db = FirebaseDatabase.getInstance().reference

        db.child("users").child(userId).get().addOnSuccessListener { snapshot ->
            val isComplete = snapshot.child("isProfileComplete")
                .getValue(Boolean::class.java) ?: false

            if (isComplete) {
                action()
            } else {
                showPopup(fragment)
            }
        }
    }

    private fun showPopup(fragment: Fragment) {
        val view = LayoutInflater.from(fragment.requireContext())
            .inflate(R.layout.pop_up_profile, null)

        val dialog = AlertDialog.Builder(fragment.requireContext())
            .setView(view)
            .setCancelable(true)
            .create()

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        view.setOnClickListener {
            dialog.dismiss()

            fragment.parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PengaturanFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}