package com.faiz.bumiloka

data class LevelModel(
    val level: Int,
    val title: String,
    val description: String,
    var isUnlocked: Boolean = false,
    var isActive: Boolean = false
)
