package com.frame.zero

import android.app.Application
import com.frame.zero.di.androidContextModule
import com.frame.zero.di.initKoin
import org.koin.core.Koin

class FrameZeroApp : Application() {
  lateinit var koin: Koin
    private set

  override fun onCreate() {
    super.onCreate()
    koin = initKoin(extraModules = listOf(androidContextModule(applicationContext)))
  }
}

val Application.koin: Koin
  get() = (this as FrameZeroApp).koin

inline fun <reified T : Any> Application.get(): T = koin.get()
