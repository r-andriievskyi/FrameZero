package com.frame.zero.core.push

/**
 * Source of the platform push (FCM) registration token. Implemented by a backend
 * integration (see `shared/integrations/firebase`) and consumed by the
 * `DeviceTokenSynchronizer`, mirroring the [com.frame.zero.core.logging.LogSink] /
 * [com.frame.zero.core.analytics.AnalyticsSink] contract-plus-impl pattern. With no
 * integration registered there is no provider and registration is simply skipped.
 *
 * Token rotation is delivered by the platform (Android's `FirebaseMessagingService.
 * onNewToken`), which forwards the new token to `DeviceTokenSynchronizer.onNewToken` —
 * GitLive's common Firebase Messaging API exposes no rotation flow.
 */
interface PushTokenProvider {
  /** The current token, or null if one isn't available yet (e.g. iOS without APNs). */
  suspend fun currentToken(): String?
}
