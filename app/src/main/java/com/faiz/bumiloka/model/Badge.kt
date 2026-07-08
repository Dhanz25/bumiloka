package com.faiz.bumiloka.model

data class Badge(
    var id: String = "",
    var nama: String = "",
    var deskripsi: String = "",
    var imageUrl: String = "", // Bisa berupa nama drawable atau Base64
    var level: Int = 1,
    var kriteria: String = "",
    var createdAt: Long = System.currentTimeMillis()
)
