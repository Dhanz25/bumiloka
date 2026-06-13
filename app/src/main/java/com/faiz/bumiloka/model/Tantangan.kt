package com.faiz.bumiloka.model

data class Tantangan(
    val id: String = "",
    val judul: String = "",
    val deskripsi: String = "",
    val materiId: String = "", // Untuk syarat: materi[materiId]_selesai
    val quizId: String = "",   // Untuk syarat: quiz[quizId]_selesai
    val badgeId: String = "",
    val aktif: Boolean = true,
    val createdAt: Long = 0L
)