package com.frame.zero.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

internal actual fun httpClientEngine(): HttpClientEngine =
  Darwin.create {
    configureRequest {
      setAllowsCellularAccess(true)
      setTimeoutInterval(30.0)
    }
  }
