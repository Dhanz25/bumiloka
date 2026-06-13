package com.faiz.bumiloka

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object LencanaHelper {

    fun tambahLencana(namaLencana: String) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val db = FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(userId)

        db.child("lencana")
            .child(namaLencana)
            .get()
            .addOnSuccessListener { snapshot ->

                // jika sudah pernah dapat
                if (snapshot.exists()) return@addOnSuccessListener

                db.child("lencana")
                    .child(namaLencana)
                    .setValue(true)

                db.child("totalLencana")
                    .get()
                    .addOnSuccessListener {

                        val total =
                            it.getValue(Int::class.java) ?: 0

                        db.child("totalLencana")
                            .setValue(total + 1)
                    }
            }
    }
}