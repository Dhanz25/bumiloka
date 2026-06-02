package com.faiz.bumiloka.model
data class Tantangan(
    val id: String = "",
    val judul: String = "",
    val deskripsi: String = "",
    val targetPoin: Int = 0,
    val hadiah: String = "",
    val tanggalMulai: Long = System.currentTimeMillis(),
    val tanggalSelesai: Long = System.currentTimeMillis(),
    val aktif: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)