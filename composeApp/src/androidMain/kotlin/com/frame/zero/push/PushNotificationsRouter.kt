package com.frame.zero.push

import android.content.Intent
import com.frame.zero.core.navigation.DeepLink
import com.frame.zero.core.navigation.NavigationSignal

/**
 * Routes a tapped task-assignment notification into a [DeepLink] on [signal].
 *
 * Parses the intent built by [FrameZeroMessagingService] — the producer of
 * [PushNotifications.EXTRA_TASK_ID] — so the activity stays agnostic of the push
 * payload. The task id extra is consumed once read, so a re-delivery of the same
 * intent (config change, `onNewIntent` replays) doesn't navigate twice.
 */
class PushNotificationsRouter(
  private val signal: NavigationSignal
) {
  fun route(intent: Intent?) {
    val taskId = intent?.getStringExtra(PushNotifications.EXTRA_TASK_ID) ?: return
    intent.removeExtra(PushNotifications.EXTRA_TASK_ID)
    signal.emit(DeepLink.TaskDetails(taskId))
  }
}
