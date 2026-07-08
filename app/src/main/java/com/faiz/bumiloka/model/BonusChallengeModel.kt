package com.faiz.bumiloka.model

data class BonusChallengeModel(
    var id: String = "",
    var judul: String = "",
    var deskripsi: String = "",
    var badgeId: String = "",
    var materiId: String = "",       // Field materiId harus ada
    var type: String = "COMMITMENT", // "COMMITMENT" atau "QUIZ"
    var targetDays: Int = 1,
    var quizId: String = "",         // quizId harus String
    var aktif: Boolean = true,
    var createdAt: Long = System.currentTimeMillis()
)
