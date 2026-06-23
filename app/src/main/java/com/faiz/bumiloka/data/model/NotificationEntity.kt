package com.faiz.bumiloka.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val category: String = "Sistem" // Kategori: Reward, Aktivitas, Sistem
)
