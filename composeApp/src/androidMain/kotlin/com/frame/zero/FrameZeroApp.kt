package com.frame.zero

import android.app.Application
import com.frame.zero.feature.initKoin
import org.koin.core.Koin

class FrameZeroApp : Application() {
  lateinit var koin: Koin
    private set

  override fun onCreate() {
    super.onCreate()
    koin = initKoin()
  }
}

val Application.koin: Koin
  get() = (this as FrameZeroApp).koin

inline fun <reified T : Any> Application.get(): T = koin.get()
