package com.frame.zero.core.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.mp.KoinPlatform
import java.util.concurrent.TimeUnit

internal actual fun httpClientEngine(): HttpClientEngine {
  val context = KoinPlatform.getKoin().get<Context>()
  return OkHttp.create {
    config {
      connectTimeout(15, TimeUnit.SECONDS)
      readTimeout(30, TimeUnit.SECONDS)
    }
    addInterceptor(ChuckerInterceptor.Builder(context).build())
  }
}
