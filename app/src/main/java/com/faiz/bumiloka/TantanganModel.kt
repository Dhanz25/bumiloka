package com.faiz.bumiloka

data class TantanganModel(
    val id: String = "",
    val judul: String = "",
    val deskripsi: String = "",
    val badgeId: Int = 0,
    val materiId: Int = 0,
    val quizId: Int = 0,
    val aktif: Boolean = true,
    var status: String = "aktif",
    val createdAt: Long = System.currentTimeMillis()
)