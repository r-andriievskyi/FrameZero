package com.frame.zero.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.mp.KoinPlatform
import java.util.concurrent.TimeUnit

internal actual fun httpClientEngine(): HttpClientEngine {
  val debugInterceptors = KoinPlatform.getKoin().getAll<HttpDebugInterceptor>()
  return OkHttp.create {
    config {
      connectTimeout(15, TimeUnit.SECONDS)
      readTimeout(30, TimeUnit.SECONDS)
    }
    debugInterceptors.forEach { addInterceptor(it) }
  }
}
