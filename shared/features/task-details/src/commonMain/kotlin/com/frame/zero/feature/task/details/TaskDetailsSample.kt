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
      role = "Director",
      avatarColorHex = "#0097A7"
    ),
    reporter = TaskMember(
      initials = "TE",
      name = "Tom Ellison",
      role = "1st AD",
      avatarColorHex = "#7B1FA2"
    ),
    dueDate = "Apr 26, 2026",
    isDueToday = true,
    phase = "Production",
    description = "Writer turned in revised pages for the confrontation in Scene 12. " +
      "Review the new dialogue against the shooting schedule and flag any continuity " +
      "issues with the Act II callbacks before the table read.",
    tags = listOf("Script", "Act II", "Review"),
    checklist = listOf(
      ChecklistItem(id = "1", text = "Read revised pages (3–7)", isCompleted = true),
      ChecklistItem(id = "2", text = "Check continuity vs. Scene 9", isCompleted = true),
      ChecklistItem(id = "3", text = "Note blocking changes for DP", isCompleted = false),
      ChecklistItem(id = "4", text = "Sign off with writer", isCompleted = false)
    ),
    attachments = listOf(
      TaskAttachment(
        id = "a1",
        fileName = "Scene12_rev4.pdf",
        fileType = "PDF",
        fileSize = "1.2 MB"
      ),
      TaskAttachment(
        id = "a2",
        fileName = "Continuity_notes.fdx",
        fileType = "Final Draft",
        fileSize = "84 KB"
      )
    ),
    activity = listOf(
      ActivityEntry(
        id = "act1",
        initials = "TE",
        avatarColorHex = "#7B1FA2",
        text = "TE assigned this to Maya",
        timestamp = "4d ago"
      ),
      ActivityEntry(
        id = "act2",
        initials = "MR",
        avatarColorHex = "#0097A7",
        text = "MR started reviewing pages",
        timestamp = "2h ago"
      )
    )
  )
