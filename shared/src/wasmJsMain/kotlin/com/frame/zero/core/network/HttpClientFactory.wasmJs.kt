package com.frame.zero.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

internal actual fun httpClientEngine(): HttpClientEngine = Js.create()
