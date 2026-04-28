package com.faiz.bumiloka

class TantanganModel (
    val challengeId: String = "penjelajah_mingguan",
    val challengeName: String = "Penjelajah Mingguan",
    val totalMateri: Int = 3,
    val materiSelesai: Int = 0,
    val kuisSelesai: Boolean = false,
    val progress: Int = 0,
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val status: String = "belum_mulai"
)