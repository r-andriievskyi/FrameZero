package com.frame.zero.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun httpClient(
  config: HttpClientConfig<*>.() -> Unit
): HttpClient = HttpClient(OkHttp) { config() }
