package com.faiz.bumiloka.model

data class Tantangan(
    var id: String = "",
    var judul: String = "",
    var deskripsi: String = "",
    var imageUrl: String = "",
    var materiId: String = "",
    var quizId: String = "",
    var badgeId: String = "",
    var level: Int = 1,
    var aktif: Boolean = true,
    var createdAt: Long = System.currentTimeMillis()
)