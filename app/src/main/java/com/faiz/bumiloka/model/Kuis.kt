package com.faiz.bumiloka.model

data class Kuis(
    var id: String = "",
    var edukasiId: String = "",
    var judul: String = "",
    var deskripsi: String = "",
    var level: Int = 1,
    var imageUrl: String = "",
    var poinReward: Int = 0,
    var aktif: Boolean = true,
    var createdAt: Long = System.currentTimeMillis()
)