package com.faiz.bumiloka // Sesuaikan dengan package kamu

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object AktivitasHelper {

    fun tambahPoint(tambahanXP: Int) {
        // Otomatis ambil userId yang sedang login
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        db.get().addOnSuccessListener { snapshot ->
            // Ambil level dan XP saat ini
            var currentLevel = snapshot.child("level").getValue(Int::class.java) ?: 1
            var currentXP = snapshot.child("xp").getValue(Int::class.java) ?: 0

            // Target XP untuk level saat ini (Kelipatan 100)
            var targetXP = currentLevel * 100

            // Tambahkan XP baru
            currentXP += tambahanXP

            // Kalkulasi naik level
            while (currentXP >= targetXP) {
                currentXP -= targetXP
                currentLevel++
                targetXP = currentLevel * 100
            }

            // Simpan data Level dan XP yang baru ke Firebase
            val updates = mapOf(
                "level" to currentLevel,
                "xp" to currentXP
            )

            db.updateChildren(updates).addOnSuccessListener {
                Log.d("BUMILOKA_LEVEL", "Berhasil update: Level $currentLevel, XP: $currentXP")
            }.addOnFailureListener {
                Log.e("BUMILOKA_LEVEL", "Gagal menyimpan XP: ${it.message}")
            }
        }.addOnFailureListener {
            Log.e("BUMILOKA_LEVEL", "Gagal membaca database: ${it.message}")
        }
    }
    fun tambahMisiSelesai() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        db.child("misiTercapai").get().addOnSuccessListener { snapshot ->
            // Ambil jumlah misi saat ini (default 0), lalu tambah 1
            val currentMisi = snapshot.getValue(Int::class.java) ?: 0
            db.child("misiTercapai").setValue(currentMisi + 1)
        }
    }

    // Fungsi untuk mendapatkan lencana baru
    // idLencana bisa diisi dengan nama unik, misal: "lencana_tumbler", "lencana_kuis1"
    fun tambahLencana(idLencana: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        // 1. Simpan nama lencananya agar tahu lencana mana yang sudah didapat
        db.child("lencana_dimiliki").child(idLencana).setValue(true)

        // 2. Tambahkan total angka lencananya
        db.child("totalLencana").get().addOnSuccessListener { snapshot ->
            val currentTotal = snapshot.getValue(Int::class.java) ?: 0
            db.child("totalLencana").setValue(currentTotal + 1)
        }
    }
}