package com.frame.zero.notification

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream

/**
 * Sends FCM messages via the Firebase Admin SDK. The [FirebaseApp] is initialised
 * lazily from the service-account JSON at [credentialsPath] (validated non-blank at
 * boot by [com.frame.zero.config.AppConfig]). Each send carries both a notification
 * (so the system tray displays it when the app is backgrounded) and a data payload
 * the client reads to deep-link.
 */
class FirebaseAdminPushSender(
  private val credentialsPath: String,
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PushSender {
  private val app: FirebaseApp by lazy {
    val options = FirebaseOptions
      .builder()
      .setCredentials(GoogleCredentials.fromStream(FileInputStream(credentialsPath)))
      .build()
    FirebaseApp.getApps().firstOrNull { it.name == APP_NAME } ?: FirebaseApp.initializeApp(options, APP_NAME)
  }

  override suspend fun sendToTokens(
    tokens: List<String>,
    title: String,
    body: String,
    data: Map<String, String>
  ) {
    if (tokens.isEmpty()) return
    withContext(ioDispatcher) {
      val message = MulticastMessage
        .builder()
        .addAllTokens(tokens)
        .setNotification(
          Notification
            .builder()
            .setTitle(title)
            .setBody(body)
            .build()
        ).putAllData(data)
        .build()
      // sendEachForMulticast reports per-token failures in its BatchResponse rather
      // than throwing, so one stale token can't sink the batch.
      FirebaseMessaging.getInstance(app).sendEachForMulticast(message)
    }
  }

  private companion object {
    const val APP_NAME = "framezero-push"
  }
}
