package com.frame.zero.feature.task.details

sealed interface TaskDetailsIntent {
  data object Refresh : TaskDetailsIntent

  data object MarkComplete : TaskDetailsIntent

  data object DownloadAttachment : TaskDetailsIntent

  data object AttachmentErrorDismissed : TaskDetailsIntent

  data object ParticipantPickerOpened : TaskDetailsIntent

  data object ParticipantPickerDismissed : TaskDetailsIntent

  data class ParticipantSearchChanged(
    val query: String
  ) : TaskDetailsIntent

  data class ParticipantToggled(
    val userId: String
  ) : TaskDetailsIntent

  data object ParticipantsErrorDismissed : TaskDetailsIntent
}
