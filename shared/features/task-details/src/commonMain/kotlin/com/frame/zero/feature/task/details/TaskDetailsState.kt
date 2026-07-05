package com.frame.zero.feature.task.details

import com.frame.zero.ui.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate

data class TaskDetailsState(
  val taskId: String = "",
  val title: String = "",
  val productionName: String = "",
  val productionId: String = "",
  val priority: TaskPriority = TaskPriority.MEDIUM,
  val status: TaskStatus = TaskStatus.IN_PROGRESS,
  val assignee: TaskMember? = null,
  val dueDate: LocalDate? = null,
  val isDueToday: Boolean = false,
  val description: String = "",
  val attachment: TaskAttachment? = null,
  val isDownloadingAttachment: Boolean = false,
  val attachmentError: AttachmentDownloadError? = null,
  val participants: ImmutableList<AssignableMemberUi> = persistentListOf(),
  val assignableMembers: ImmutableList<AssignableMemberUi> = persistentListOf(),
  val isParticipantPickerVisible: Boolean = false,
  val participantQuery: String = "",
  val isUpdatingParticipants: Boolean = false,
  val participantsError: UiText? = null,
  val isLoading: Boolean = false,
  val isError: Boolean = false,
  val showMarkCompleteButton: Boolean = false,
  /** Unread messages in the task chat; 0 hides the badge on the chat entry point. */
  val unreadChatCount: Int = 0
) {
  /** Members matching the current search query — drives the participants bottom sheet list. */
  val filteredAssignableMembers: ImmutableList<AssignableMemberUi>
    get() {
      val query = participantQuery.trim()
      return if (query.isEmpty()) {
        assignableMembers
      } else {
        assignableMembers.filter { it.name.contains(query, ignoreCase = true) }.toImmutableList()
      }
    }
}

enum class TaskPriority { HIGH, MEDIUM, LOW }

enum class TaskStatus { IN_PROGRESS, COMPLETED }

/** Reason an attachment download couldn't proceed; the UI maps it to a message. */
enum class AttachmentDownloadError { OFFLINE, INSUFFICIENT_STORAGE, GENERIC }

data class TaskAttachment(
  val fileName: String,
  val typeLabel: String,
  val sizeLabel: String,
  val contentType: String,
  val sizeBytes: Long
)

data class TaskMember(
  val initials: String,
  val name: String,
  val avatarColorHex: String? = null
)
