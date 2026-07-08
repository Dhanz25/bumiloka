package com.faiz.bumiloka.model

data class Tantangan(
    var id: String = "",
    var judul: String = "",
    var deskripsi: String = "",
    var imageUrl: String = "",
    var materiId: String = "",
    var quizId: String = "",
    var type: String = "SINGLE", // SINGLE (1 Materi & 1 Kuis), QUIZ_COUNT (N Kuis), MATERI_COUNT (N Materi)
    var targetCount: Int = 1,
    var badgeId: String = "",
    var level: Int = 1,
    var aktif: Boolean = true,
    var createdAt: Long = System.currentTimeMillis()
)
