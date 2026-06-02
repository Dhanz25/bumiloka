package com.faiz.bumiloka.model
data class Edukasi(
    val id: String = "",
    val judul: String = "",
    val konten: String = "",
    val kategori: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)