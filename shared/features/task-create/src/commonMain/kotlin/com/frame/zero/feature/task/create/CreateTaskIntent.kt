package com.frame.zero.feature.task.create

import com.frame.zero.domain.task.TaskPriority
import kotlinx.datetime.LocalDate

sealed interface CreateTaskIntent {
  data class TitleChanged(
    val title: String
  ) : CreateTaskIntent

  data class DescriptionChanged(
    val description: String
  ) : CreateTaskIntent

  data object AssigneePickerOpened : CreateTaskIntent

  data object AssigneePickerDismissed : CreateTaskIntent

  data class AssigneeSearchChanged(
    val query: String
  ) : CreateTaskIntent

  data class AssigneeSelected(
    val userId: String?
  ) : CreateTaskIntent

  data object ParticipantPickerOpened : CreateTaskIntent

  data object ParticipantPickerDismissed : CreateTaskIntent

  data class ParticipantSearchChanged(
    val query: String
  ) : CreateTaskIntent

  data class ParticipantToggled(
    val userId: String
  ) : CreateTaskIntent

  data class PriorityChanged(
    val priority: TaskPriority
  ) : CreateTaskIntent

  data class DueDateChanged(
    val date: LocalDate?
  ) : CreateTaskIntent

  data class QuickDueDateSelected(
    val option: DueDateQuickOption
  ) : CreateTaskIntent

  data object AttachFileClicked : CreateTaskIntent

  data object AttachmentRemoved : CreateTaskIntent

  data object Submit : CreateTaskIntent

  data object ToastDismissed : CreateTaskIntent
}

enum class DueDateQuickOption {
  TODAY,
  TOMORROW,
  THIS_WEEK,
  NEXT_WEEK
}
