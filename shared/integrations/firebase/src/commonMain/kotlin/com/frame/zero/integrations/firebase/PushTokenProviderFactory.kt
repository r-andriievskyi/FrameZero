package com.frame.zero.integrations.firebase

import com.frame.zero.core.push.PushTokenProvider

/**
 * Builds the platform [PushTokenProvider]. Android reads the FCM token via the GitLive
 * Firebase Messaging SDK; iOS returns a no-op until APNs is wired up (see the
 * Android-first scope), so the synchronizer simply skips registration there.
 */
internal expect fun firebasePushTokenProvider(): PushTokenProvider
