package com.faiz.bumiloka.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.faiz.bumiloka.MainActivity
import com.faiz.bumiloka.R
import com.faiz.bumiloka.data.local.NotificationDatabase
import com.faiz.bumiloka.data.model.NotificationEntity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "BumiLoka"
        val body = remoteMessage.notification?.body ?: ""
        val timestamp = System.currentTimeMillis()

        // Tampilkan Notifikasi di Status Bar
        sendNotification(title, body)

        // Simpan ke Database Room
        saveToDatabase(title, body, timestamp)
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "bumiloka_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "BumiLoka News",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun saveToDatabase(title: String, body: String, timestamp: Long) {
        serviceScope.launch {
            val database = NotificationDatabase.getDatabase(applicationContext)
            database.notificationDao().insertNotification(
                NotificationEntity(title = title, body = body, timestamp = timestamp)
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token baru bisa dikirim ke server jika diperlukan
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
