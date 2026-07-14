package com.frame.zero

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.frame.zero.core.security.ActivityHolder
import com.frame.zero.core.security.AppLifecycleObserver
import com.frame.zero.di.androidContextModule
import com.frame.zero.di.initKoin
import com.frame.zero.push.PushNotifications
import org.koin.core.Koin

class FrameZeroApp : Application() {
  lateinit var koin: Koin
    private set

  override fun onCreate() {
    super.onCreate()
    koin = initKoin(extraModules = listOf(androidContextModule(applicationContext)))
    // lets the biometric authenticator find the foreground activity to host its prompt.
    koin.get<ActivityHolder>().attachTo(this)
    // re-locks the session on real backgrounding (ignores config-change recreation).
    koin.get<AppLifecycleObserver>().attachTo(this)
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

val Application.koin: Koin
  get() = (this as FrameZeroApp).koin

inline fun <reified T : Any> Application.get(): T = koin.get()
