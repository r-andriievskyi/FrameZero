package com.frame.zero.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine

internal expect fun httpClientEngine(): HttpClientEngine

internal fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient =
  HttpClient(httpClientEngine()) { config() }
