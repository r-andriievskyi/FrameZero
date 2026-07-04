package com.frame.zero.common

import java.util.UUID

/**
 * Hook the task layer calls after a task's circle changes (assignee reassigned or
 * participants replaced), so a live chat hub can drop subscriptions of users who
 * are no longer in the circle. Kept as an interface in `common` — with a [NONE]
 * no-op — so the task module never depends on chat internals and tests that don't
 * care about chat can pass the no-op.
 */
fun interface TaskCircleRevocationListener {
  suspend fun onTaskCircleChanged(
    taskId: UUID,
    circleUserIds: Set<UUID>
  )

  companion object {
    val NONE = TaskCircleRevocationListener { _, _ -> }
  }
}
