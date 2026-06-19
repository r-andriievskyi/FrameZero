package com.frame.zero.push

object PushNotifications {
  const val CHANNEL_ID = "task_assignments"
  const val CHANNEL_NAME = "Task assignments"

  /** Key under which a tapped notification carries the task id into [com.frame.zero.MainActivity]. */
  const val EXTRA_TASK_ID = "taskId"

  /** Key the server sets in the FCM data payload (see TaskAssignmentNotifier). */
  const val DATA_TASK_ID = "taskId"
}
