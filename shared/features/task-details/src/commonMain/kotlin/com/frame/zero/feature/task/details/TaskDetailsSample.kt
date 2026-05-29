package com.frame.zero.feature.task.details

fun sampleTaskDetailsState(taskId: String = "preview-task-id"): TaskDetailsState =
  TaskDetailsState(
    taskId = taskId,
    title = "Review Scene 12 script revisions",
    productionName = "Echoes of Silence",
    priority = TaskPriority.HIGH,
    status = TaskStatus.IN_PROGRESS,
    assignee = TaskMember(
      initials = "MR",
      name = "Maya Rivera",
      avatarColorHex = "#0097A7"
    ),
    dueDate = "Apr 26, 2026",
    isDueToday = true,
    description = "Writer turned in revised pages for the confrontation in Scene 12. " +
      "Review the new dialogue against the shooting schedule and flag any continuity " +
      "issues with the Act II callbacks before the table read."
  )
