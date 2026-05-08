package com.frame.zero.core.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.mp.KoinPlatform

internal actual fun httpClientEngine(): HttpClientEngine {
  val context = KoinPlatform.getKoin().get<Context>()
  return OkHttp.create {
    addInterceptor(ChuckerInterceptor.Builder(context).build())
  }
}
