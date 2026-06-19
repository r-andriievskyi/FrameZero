package com.frame.zero.integrations.firebase

import com.frame.zero.core.push.PushTokenProvider

// iOS push is not wired yet (no APNs setup) — return no token so registration is a
// no-op rather than failing. Replace with a GitLive/native Messaging-backed provider
// when iOS push is implemented.
internal actual fun firebasePushTokenProvider(): PushTokenProvider = NoOpPushTokenProvider

private object NoOpPushTokenProvider : PushTokenProvider {
  override suspend fun currentToken(): String? = null
}
