package com.faiz.bumiloka.model

data class Edukasi(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var content: String = "",
    var imageUrl: String = "",
    var badgeName: String = "",
    var badgeImage: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var aktif: Boolean = true
)