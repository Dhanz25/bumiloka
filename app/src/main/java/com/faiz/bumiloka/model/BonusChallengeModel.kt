package com.faiz.bumiloka.model

data class BonusChallengeModel(
    val id: String = "",
    val judul: String = "",
    val deskripsi: String = "",
    val badgeId: String = "",
    val materiId: Int = 0,
    val quizId: Int = 0,
    val aktif: Boolean = true
)
