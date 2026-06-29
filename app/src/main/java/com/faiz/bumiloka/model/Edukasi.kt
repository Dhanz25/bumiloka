package com.faiz.bumiloka.model

data class Edukasi(
    var id: String = "",
    var kuisId: String = "", // Menghubungkan ke Kuis tertentu
    var level: Int = 1,
    var title: String = "",
    var description: String = "",
    var content: String = "", // Legacy support / Section 1 alias
    var imageUrl: String = "",
    
    // Legacy fields for compatibility
    var isiTitle: String = "",
    var pentingTitle: String = "",
    var pentingContent: String = "",
    var contohTitle: String = "",
    var contohContent: String = "",
    
    // New Section fields
    var section1Title: String = "",
    var section1Content: String = "",
    var section2Title: String = "",
    var section2Content: String = "",
    var section3Title: String = "",
    var section3Content: String = "",
    
    var createdAt: Long = System.currentTimeMillis(),
    var aktif: Boolean = true
)
