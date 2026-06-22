package com.faiz.bumiloka.data.repository

import com.faiz.bumiloka.data.local.NotificationDao
import com.faiz.bumiloka.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val notificationDao: NotificationDao) {
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val unreadCount: Flow<Int> = notificationDao.getUnreadCount()

    suspend fun insert(notification: NotificationEntity) = notificationDao.insertNotification(notification)
    suspend fun markAsRead(id: Int) = notificationDao.markAsRead(id)
    suspend fun markAllAsRead() = notificationDao.markAllAsRead()
    suspend fun delete(notification: NotificationEntity) = notificationDao.deleteNotification(notification)
    suspend fun clearAll() = notificationDao.deleteAllNotifications()
}
