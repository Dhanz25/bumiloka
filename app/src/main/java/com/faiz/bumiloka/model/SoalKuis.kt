package com.faiz.bumiloka.model

data class SoalKuis(
    var id: String = "",
    var pertanyaan: String = "",
    var imageUrl: String = "", // Optional image for the question
    var opsiA: String = "",
    var opsiB: String = "",
    var opsiC: String = "",
    var opsiD: String = "",
    var jawabanBenar: String = "" // A, B, C, or D
)