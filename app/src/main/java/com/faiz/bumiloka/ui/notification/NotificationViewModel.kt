package com.faiz.bumiloka.ui.notification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.faiz.bumiloka.data.local.NotificationDatabase
import com.faiz.bumiloka.data.model.NotificationEntity
import com.faiz.bumiloka.data.repository.NotificationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NotificationRepository
    
    private val _currentCategory = MutableStateFlow("Semua")
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val notifications: StateFlow<List<NotificationEntity>> = _currentCategory
        .flatMapLatest { category ->
            if (category == "Semua") {
                repository.allNotifications
            } else {
                repository.getNotificationsByCategory(category)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val unreadCount: StateFlow<Int>

    init {
        val dao = NotificationDatabase.getDatabase(application).notificationDao()
        repository = NotificationRepository(dao)
        unreadCount = repository.unreadCount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )
    }

    fun setCategory(category: String) {
        _currentCategory.value = category
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
