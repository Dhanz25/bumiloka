package com.faiz.bumiloka.ui.notification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.faiz.bumiloka.data.local.NotificationDatabase
import com.faiz.bumiloka.data.model.NotificationEntity
import com.faiz.bumiloka.data.repository.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NotificationRepository
    val notifications: StateFlow<List<NotificationEntity>>
    val unreadCount: StateFlow<Int>

    init {
        val dao = NotificationDatabase.getDatabase(application).notificationDao()
        repository = NotificationRepository(dao)
        notifications = repository.allNotifications.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        unreadCount = repository.unreadCount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )
    }

    fun markAsRead(id: Int) = viewModelScope.launch {
        repository.markAsRead(id)
    }

    fun markAllAsRead() = viewModelScope.launch {
        repository.markAllAsRead()
    }

    fun deleteNotification(notification: NotificationEntity) = viewModelScope.launch {
        repository.delete(notification)
    }

    fun clearAll() = viewModelScope.launch {
        repository.clearAll()
    }
}
