package com.frame.zero.feature.task.create

import com.frame.zero.domain.task.AssignableMember
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.ui.UiText
import kotlinx.datetime.LocalDate

data class CreateTaskState(
  val productionTitle: String = "",
  val title: String = "",
  val description: String = "",
  val assignableMembers: List<AssignableMember> = emptyList(),
  val assigneeUserId: String? = null,
  val isAssigneePickerVisible: Boolean = false,
  val assigneeQuery: String = "",
  val priority: TaskPriority = TaskPriority.MEDIUM,
  val dueDate: LocalDate? = null,
  val isLoading: Boolean = false,
  val titleError: UiText? = null,
  val errorToast: UiText? = null
) {
  val canSubmit: Boolean
    get() = title.isNotBlank() && !isLoading

  val selectedAssignee: AssignableMember?
    get() = assignableMembers.firstOrNull { it.userId == assigneeUserId }

  /** Members matching the current search query — drives the assignee bottom sheet list. */
  val filteredAssignableMembers: List<AssignableMember>
    get() {
      val query = assigneeQuery.trim()
      return if (query.isEmpty()) {
        assignableMembers
      } else {
        assignableMembers.filter { it.name.contains(query, ignoreCase = true) }
      }
    }
}
