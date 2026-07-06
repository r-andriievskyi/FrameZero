package com.frame.zero.feature.task.create

import com.frame.zero.core.files.PickedFile
import com.frame.zero.domain.task.TaskPriority
import com.frame.zero.ui.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate

data class CreateTaskState(
  val productionTitle: String = "",
  val title: String = "",
  val description: String = "",
  val assignableMembers: ImmutableList<AssignableMemberUi> = persistentListOf(),
  val assigneeUserId: String? = null,
  val isAssigneePickerVisible: Boolean = false,
  val assigneeQuery: String = "",
  val participantUserIds: ImmutableList<String> = persistentListOf(),
  val isParticipantPickerVisible: Boolean = false,
  val participantQuery: String = "",
  val priority: TaskPriority = TaskPriority.MEDIUM,
  val dueDate: LocalDate? = null,
  val attachment: PickedFile? = null,
  val isLoading: Boolean = false,
  val titleError: UiText? = null,
  val attachmentError: UiText? = null,
  val errorToast: UiText? = null
) {
  val canSubmit: Boolean
    get() = title.isNotBlank() && !isLoading

  val selectedAssignee: AssignableMemberUi?
    get() = assignableMembers.firstOrNull { it.userId == assigneeUserId }

  /** Members matching the current search query — drives the assignee bottom sheet list. */
  val filteredAssignableMembers: ImmutableList<AssignableMemberUi>
    get() = membersMatching(assigneeQuery)

  /** Members selected as participants, in the order the production lists them. */
  val selectedParticipants: ImmutableList<AssignableMemberUi>
    get() = assignableMembers.filter { it.userId in participantUserIds }.toImmutableList()

  /** Members matching the participant search query — drives the participants bottom sheet list. */
  val filteredParticipantMembers: ImmutableList<AssignableMemberUi>
    get() = membersMatching(participantQuery)

  private fun membersMatching(rawQuery: String): ImmutableList<AssignableMemberUi> {
    val query = rawQuery.trim()
    return if (query.isEmpty()) {
      assignableMembers
    } else {
      assignableMembers.filter { it.name.contains(query, ignoreCase = true) }.toImmutableList()
    }
  }
}
