package com.faiz.bumiloka.model

data class UserModel(
    val uid: String = "",
    val nama: String = "",
    val email: String = "",
    val role: String = "user",
    val totalPoin: Int = 0,
    val kuisSelesai: Int = 0,
    val edukasiDibaca: Int = 0,
    val tantanganSelesai: Int = 0
)