package com.faiz.bumiloka.model

data class Notification(
    val icon: Int,
    val title: String,
    val message: String,
    val time: String,
    val isRead: Boolean
)
