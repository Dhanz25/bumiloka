package com.faiz.bumiloka.model

data class Kuis(
    var id: String = "",
    var pertanyaan: String = "",
    var opsiA: String = "",
    var opsiB: String = "",
    var opsiC: String = "",
    var opsiD: String = "",
    var jawabanBenar: String = "",
    var kategori: String = "",
    var poin: Int = 10,
    var createdAt: Long = System.currentTimeMillis()
)
