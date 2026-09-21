package com.frame.zero

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.Configuration
import com.frame.zero.di.AndroidAppGraph
import com.frame.zero.di.createAndroidAppGraph
import com.frame.zero.di.debugHttpInterceptors
import com.frame.zero.push.PushNotifications

class FrameZeroApp :
  Application(),
  Configuration.Provider {
  lateinit var graph: AndroidAppGraph
    private set

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder().setWorkerFactory(graph.taskUploadWorkerFactory).build()

  override fun onCreate() {
    super.onCreate()
    graph = createAndroidAppGraph(applicationContext, debugHttpInterceptors(applicationContext))
    // lets the biometric authenticator find the foreground activity to host its prompt.
    graph.activityHolder.attachTo(this)
    // re-locks the session on real backgrounding (ignores config-change recreation).
    graph.appLifecycleObserver.attachTo(this)
    // Metro has no eager bindings: resolve the synchronizer so it starts observing the session
    // now rather than whenever something first asks for it (Koin: `createdAtStart = true`).
    graph.deviceTokenSynchronizer
    createNotificationChannel()
  }

  private fun createNotificationChannel() {
    val channel = NotificationChannel(
      PushNotifications.CHANNEL_ID,
      PushNotifications.CHANNEL_NAME,
      NotificationManager.IMPORTANCE_HIGH
    )
    getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
  }
}

val Application.graph: AndroidAppGraph
  get() = (this as FrameZeroApp).graph
