package com.frame.zero.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine

internal fun httpClient(
  engine: HttpClientEngine,
  config: HttpClientConfig<*>.() -> Unit = {}
): HttpClient = HttpClient(engine) { config() }
