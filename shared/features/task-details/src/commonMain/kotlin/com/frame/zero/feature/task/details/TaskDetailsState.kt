@file:OptIn(ExperimentalObjCName::class)

package com.frame.zero.feature.task.details

import com.frame.zero.ui.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

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
  // `description` collides with NSObject's on iOS.
  @ObjCName("taskDescription")
  val description: String = "",
  val attachment: TaskAttachment? = null,
  val isDownloadingAttachment: Boolean = false,
  val attachmentError: UiText? = null,
  val participants: ImmutableList<AssignableMemberUi> = persistentListOf(),
  val assignableMembers: ImmutableList<AssignableMemberUi> = persistentListOf(),
  val isParticipantPickerVisible: Boolean = false,
  val participantQuery: String = "",
  val isUpdatingParticipants: Boolean = false,
  val participantsError: UiText? = null,
  val isLoading: Boolean = false,
  val error: UiText? = null,
  val markCompleteError: UiText? = null,
  val showMarkCompleteButton: Boolean = false
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

// Explicit Swift names: these collide with domain/task-create types of the same name, and the
// suffix SKIE would add instead depends on compile order.
@ObjCName("TaskDetailsPriority")
enum class TaskPriority { HIGH, MEDIUM, LOW }

@ObjCName("TaskDetailsStatus")
enum class TaskStatus { IN_PROGRESS, COMPLETED }

@ObjCName("TaskDetailsAttachment")
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
