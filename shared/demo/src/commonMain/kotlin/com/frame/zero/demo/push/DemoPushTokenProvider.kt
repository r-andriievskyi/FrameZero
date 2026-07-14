package com.frame.zero.demo.push

import com.frame.zero.core.push.PushTokenProvider

internal class DemoPushTokenProvider : PushTokenProvider {
  override suspend fun currentToken(): String? = null
}
