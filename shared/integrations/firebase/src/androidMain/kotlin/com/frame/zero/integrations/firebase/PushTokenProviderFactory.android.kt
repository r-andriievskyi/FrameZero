package com.frame.zero.integrations.firebase

import com.frame.zero.core.push.PushTokenProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.messaging.messaging

internal actual fun firebasePushTokenProvider(): PushTokenProvider = FirebaseTokenProvider()

/**
 * Reads the current FCM registration token via the GitLive Firebase Messaging SDK.
 * [currentToken] is best-effort — during early startup it can fail, so the failure is
 * swallowed and null returned, leaving the synchronizer to try again on the next login.
 */
internal class FirebaseTokenProvider : PushTokenProvider {
  override suspend fun currentToken(): String? = runCatching { Firebase.messaging.getToken() }.getOrNull()
}
