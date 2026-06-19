package com.frame.zero.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Sends a push notification to the assignee of a task. Resilient by design: failures
 * (no tokens, FCM down) are logged and swallowed so a push problem never fails the
 * caller. Call this *after* the task transaction has committed.
 *
 * The send is fire-and-forget: [notifyTaskAssigned] returns immediately and the token
 * lookup + FCM round-trip run on [scope], so task creation/update latency is never
 * coupled to FCM availability.
 */
class TaskAssignmentNotifier(
  private val deviceTokens: DeviceTokenRepository,
  private val pushSender: PushSender,
  private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
  private val log = LoggerFactory.getLogger(TaskAssignmentNotifier::class.java)

  fun notifyTaskAssigned(
    assigneeUserId: UUID,
    taskId: UUID,
    taskTitle: String
  ) {
    scope.launch {
      runCatching {
        val tokens = deviceTokens.findTokensForUser(assigneeUserId)
        if (tokens.isEmpty()) return@launch
        pushSender.sendToTokens(
          tokens = tokens,
          //todo title
          title = "New task assigned",
          body = taskTitle,
          data =
            mapOf(
              "type" to TASK_ASSIGNED,
              "taskId" to taskId.toString()
            )
        )
      }.onFailure { log.warn("Failed to send task-assignment push for task {}", taskId, it) }
    }
  }

  companion object {
    const val TASK_ASSIGNED = "TASK_ASSIGNED"
  }
}
