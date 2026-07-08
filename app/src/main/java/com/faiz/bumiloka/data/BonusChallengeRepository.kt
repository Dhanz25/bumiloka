package com.faiz.bumiloka.data

import com.faiz.bumiloka.model.BonusChallengeModel
import com.faiz.bumiloka.model.ChallengeProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object BonusChallengeRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // PASTIKAN NAMA NODE SAMA DENGAN ADMIN: "bonus_tantangan"
    private const val NODE_NAME = "bonus_tantangan"

    fun getActiveChallenges(onResult: (List<BonusChallengeModel>) -> Unit) {
        database.getReference(NODE_NAME)
            .orderByChild("aktif")
            .equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val challenges = snapshot.children.mapNotNull { child ->
                        child.getValue(BonusChallengeModel::class.java)?.apply { id = child.key ?: "" }
                    }
                    onResult(challenges)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getChallengeProgress(challengeId: String, onResult: (ChallengeProgress) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("users/$uid/challengeProgress/$challengeId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val progress = snapshot.getValue(ChallengeProgress::class.java) ?: ChallengeProgress()
                    onResult(progress)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun updateMateriDone(challengeId: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("users/$uid/challengeProgress/$challengeId/materiDone").setValue(true)
    }

    fun updateQuizDone(challengeId: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("users/$uid/challengeProgress/$challengeId/quizDone").setValue(true)
    }

    fun markAsCompleted(challengeId: String, onComplete: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("users/$uid/challengeProgress/$challengeId/completed").setValue(true)
            .addOnSuccessListener { onComplete() }
    }
}
