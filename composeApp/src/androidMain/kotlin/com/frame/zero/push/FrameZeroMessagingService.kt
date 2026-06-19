package com.frame.zero.push

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.frame.zero.FrameZeroApp
import com.frame.zero.R
import com.frame.zero.MainActivity
import com.frame.zero.repository.device_token.DeviceTokenSynchronizer
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class FrameZeroMessagingService : FirebaseMessagingService() {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  @Deprecated("Deprecated. Move to the onRegistered model")
  override fun onNewToken(token: String) {
    val synchronizer = (application as FrameZeroApp).koin.get<DeviceTokenSynchronizer>()
    scope.launch { synchronizer.onNewToken(token) }
  }

  override fun onMessageReceived(message: RemoteMessage) {
    val taskId = message.data[PushNotifications.DATA_TASK_ID]
    val title = message.notification?.title ?: "New task assigned"
    val body = message.notification?.body.orEmpty()
    showNotification(title, body, taskId)
  }

  private fun showNotification(
    title: String,
    body: String,
    taskId: String?
  ) {
    val intent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      taskId?.let { putExtra(PushNotifications.EXTRA_TASK_ID, it) }
    }
    val notificationId = taskId?.hashCode() ?: System.currentTimeMillis().toInt()
    val pendingIntent = PendingIntent.getActivity(
      this,
      notificationId,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val notification = NotificationCompat.Builder(this, PushNotifications.CHANNEL_ID)
      // A monochrome silhouette; the launcher icon would render as a white square.
      .setSmallIcon(R.drawable.ic_launcher_monochrome)
      .setContentTitle(title)
      .setContentText(body)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()
    // No-ops without POST_NOTIFICATIONS on API 33+; MainActivity requests it.
    if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
      NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    scope.cancel()
  }
}
