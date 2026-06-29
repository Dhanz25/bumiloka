package com.faiz.bumiloka.data.repository

import com.faiz.bumiloka.model.Edukasi
import com.faiz.bumiloka.model.Kuis
import com.faiz.bumiloka.model.SoalKuis
import com.faiz.bumiloka.model.Tantangan
import com.google.firebase.database.*

class AdminRepository {
    private val db = FirebaseDatabase.getInstance().reference

    // EDUKASI
    fun getEdukasi(onResult: (List<Edukasi>) -> Unit) {
        db.child("edukasi").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Edukasi>()
                snapshot.children.forEach { child ->
                    if (child.value is Map<*, *>) {
                        try {
                            child.getValue(Edukasi::class.java)?.let {
                                it.id = child.key ?: ""
                                list.add(it)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
                    if (child.value is Map<*, *>) {
                        try {
                            child.getValue(Kuis::class.java)?.let {
                                it.id = child.key ?: ""
                                list.add(it)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                onResult(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun saveKuis(kuis: Kuis, onComplete: (Boolean) -> Unit) {
        val kuisId = if (kuis.id.isEmpty()) db.child("kuis").push().key ?: "" else kuis.id
        kuis.id = kuisId
        
        db.child("kuis").child(kuisId).setValue(kuis).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun saveKuisLengkap(kuis: Kuis, soalList: List<SoalKuis>, onComplete: (Boolean) -> Unit) {
        val kuisId = if (kuis.id.isEmpty()) db.child("kuis").push().key ?: "" else kuis.id
        kuis.id = kuisId
        
        val updates = HashMap<String, Any>()
        updates["id"] = kuis.id
        updates["edukasiId"] = kuis.edukasiId
        updates["judul"] = kuis.judul
        updates["deskripsi"] = kuis.deskripsi
        updates["level"] = kuis.level
        updates["imageUrl"] = kuis.imageUrl
        updates["poinReward"] = kuis.poinReward
        updates["aktif"] = kuis.aktif
        updates["createdAt"] = kuis.createdAt

        val soalMap = HashMap<String, Any>()
        soalList.forEachIndexed { index, soal ->
            val soalId = if (soal.id.isEmpty()) "soal_${index + 1}" else soal.id
            soal.id = soalId
            soalMap[soalId] = soal
        }
        updates["soal"] = soalMap

        db.child("kuis").child(kuisId).updateChildren(updates).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun deleteKuis(id: String, onComplete: (Boolean) -> Unit) {
        db.child("kuis").child(id).removeValue().addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // TANTANGAN
    fun getTantangan(onResult: (List<Tantangan>) -> Unit) {
        db.child("tantangan").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Tantangan>()
                snapshot.children.forEach { child ->
                    if (child.value is Map<*, *>) {
                        try {
                            child.getValue(Tantangan::class.java)?.let {
                                it.id = child.key ?: ""
                                list.add(it)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                onResult(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun saveTantangan(tantangan: Tantangan, onComplete: (Boolean) -> Unit) {
        val ref = if (tantangan.id.isEmpty()) db.child("tantangan").push() else db.child("tantangan").child(tantangan.id)
        if (tantangan.id.isEmpty()) tantangan.id = ref.key ?: ""
        ref.setValue(tantangan).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun deleteTantangan(id: String, onComplete: (Boolean) -> Unit) {
        db.child("tantangan").child(id).removeValue().addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // SOAL
    fun getSoal(kuisId: String, onResult: (List<SoalKuis>) -> Unit) {
        db.child("kuis").child(kuisId).child("soal").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<SoalKuis>()
                snapshot.children.forEach { child ->
                    if (child.value is Map<*, *>) {
                        try {
                            child.getValue(SoalKuis::class.java)?.let {
                                it.id = child.key ?: ""
                                list.add(it)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
        nodes.forEach { node ->
            db.child(node).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    stats[node] = snapshot.childrenCount
                    
                    if (node == "kuis") {
                        var totalSoal = 0L
                        snapshot.children.forEach { kuisChild ->
                            totalSoal += kuisChild.child("soal").childrenCount
                        }
                        stats["soal"] = totalSoal
                    }

                    if (stats.size >= 4) {
                        onResult(stats)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
}