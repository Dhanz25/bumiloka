package com.faiz.bumiloka.data.repository

import com.faiz.bumiloka.model.Edukasi
import com.faiz.bumiloka.model.Kuis
import com.faiz.bumiloka.model.SoalKuis
import com.google.firebase.database.*

class AdminRepository {
    private val db = FirebaseDatabase.getInstance().reference

    // EDUKASI
    fun getEdukasi(onResult: (List<Edukasi>) -> Unit) {
        db.child("edukasi").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Edukasi>()
                snapshot.children.forEach { child ->
                    try {
                        child.getValue(Edukasi::class.java)?.let {
                            it.id = child.key ?: ""
                            list.add(it)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                onResult(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun saveEdukasi(edukasi: Edukasi, onComplete: (Boolean) -> Unit) {
        val ref = if (edukasi.id.isEmpty()) db.child("edukasi").push() else db.child("edukasi").child(edukasi.id)
        if (edukasi.id.isEmpty()) edukasi.id = ref.key ?: ""
        ref.setValue(edukasi).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun deleteEdukasi(id: String, onComplete: (Boolean) -> Unit) {
        db.child("edukasi").child(id).removeValue().addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // KUIS
    fun getKuis(onResult: (List<Kuis>) -> Unit) {
        db.child("kuis").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Kuis>()
                snapshot.children.forEach { child ->
                    try {
                        // Cek apakah child adalah sebuah object (bukan String)
                        if (child.value is Map<*, *>) {
                            child.getValue(Kuis::class.java)?.let {
                                it.id = child.key ?: ""
                                list.add(it)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                onResult(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun saveKuis(kuis: Kuis, onComplete: (Boolean) -> Unit) {
        val ref = if (kuis.id.isEmpty()) db.child("kuis").push() else db.child("kuis").child(kuis.id)
        if (kuis.id.isEmpty()) kuis.id = ref.key ?: ""
        ref.setValue(kuis).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun deleteKuis(id: String, onComplete: (Boolean) -> Unit) {
        db.child("kuis").child(id).removeValue().addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // SOAL
    fun getSoal(kuisId: String, onResult: (List<SoalKuis>) -> Unit) {
        db.child("kuis").child(kuisId).child("soal").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<SoalKuis>()
                snapshot.children.forEach { child ->
                    try {
                        if (child.value is Map<*, *>) {
                            child.getValue(SoalKuis::class.java)?.let {
                                it.id = child.key ?: ""
                                list.add(it)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                onResult(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun saveSoal(kuisId: String, soal: SoalKuis, onComplete: (Boolean) -> Unit) {
        val ref = if (soal.id.isEmpty()) db.child("kuis").child(kuisId).child("soal").push() else db.child("kuis").child(kuisId).child("soal").child(soal.id)
        if (soal.id.isEmpty()) soal.id = ref.key ?: ""
        ref.setValue(soal).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun deleteSoal(kuisId: String, soalId: String, onComplete: (Boolean) -> Unit) {
        db.child("kuis").child(kuisId).child("soal").child(soalId).removeValue().addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // STATISTIK
    fun getStatistik(onResult: (Map<String, Long>) -> Unit) {
        val stats = mutableMapOf<String, Long>()
        val nodes = listOf("edukasi", "kuis", "users", "tantangan")
        var count = 0
        nodes.forEach { node ->
            db.child(node).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    stats[node] = snapshot.childrenCount
                    
                    // Specific logic for "soal" - it's nested in kuis
                    if (node == "kuis") {
                        var totalSoal = 0L
                        snapshot.children.forEach { kuisChild ->
                            totalSoal += kuisChild.child("soal").childrenCount
                        }
                        stats["soal"] = totalSoal
                    }

                    if (stats.size >= 5) { // edukasi, kuis, users, tantangan, soal
                        onResult(stats)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
}
