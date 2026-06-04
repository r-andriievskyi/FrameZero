package com.frame.zero.feature.task.details

import com.frame.zero.dto.task.TaskAssigneeDto
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskPriority as DtoTaskPriority
import com.frame.zero.dto.task.TaskStatus as DtoTaskStatus
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class TaskDetailMapperTest {
  private val today = LocalDate(2026, 4, 26)

  private fun dto(
    status: DtoTaskStatus = DtoTaskStatus.OPEN,
    priority: DtoTaskPriority = DtoTaskPriority.MEDIUM,
    dueDate: LocalDate? = null,
    description: String? = "desc",
    assignee: TaskAssigneeDto? = null
  ) = TaskDetailDto(
    id = "t1",
    productionId = "p1",
    productionTitle = "Echoes of Silence",
    title = "Review",
    description = description,
    dueDate = dueDate,
    status = status,
    priority = priority,
    assigneeUserId = assignee?.userId,
    assignee = assignee,
    createdAt = Instant.fromEpochMilliseconds(0L)
  )

  @Test
  fun `formats due date as abbreviated month day year`() {
    val state = dto(dueDate = LocalDate(2026, 4, 6)).toTaskDetailsState(today)
    assertEquals("Apr 6, 2026", state.dueDate)
  }

  @Test
  fun `flags due today when date equals today`() {
    val state = dto(dueDate = today).toTaskDetailsState(today)
    assertTrue(state.isDueToday)
  }

  @Test
  fun `not due today when date differs`() {
    val state = dto(dueDate = LocalDate(2026, 4, 27)).toTaskDetailsState(today)
    assertFalse(state.isDueToday)
  }

  @Test
  fun `derives two-letter initials from assignee name`() {
    val state = dto(
      assignee = TaskAssigneeDto(userId = "u1", name = "maya rivera", avatarColorHex = "#fff")
    ).toTaskDetailsState(today)
    assertEquals("MR", state.assignee?.initials)
    assertEquals("maya rivera", state.assignee?.name)
    assertEquals("#fff", state.assignee?.avatarColorHex)
  }

  @Test
  fun `null assignee maps to null member`() {
    assertNull(dto(assignee = null).toTaskDetailsState(today).assignee)
  }

  @Test
  fun `open maps to in progress with visible button`() {
    val state = dto(status = DtoTaskStatus.OPEN).toTaskDetailsState(today)
    assertEquals(TaskStatus.IN_PROGRESS, state.status)
    assertTrue(state.showMarkCompleteButton)
  }

  @Test
  fun `done maps to completed with hidden button`() {
    val state = dto(status = DtoTaskStatus.DONE).toTaskDetailsState(today)
    assertEquals(TaskStatus.COMPLETED, state.status)
    assertFalse(state.showMarkCompleteButton)
  }

  @Test
  fun `null description maps to empty string`() {
    assertEquals("", dto(description = null).toTaskDetailsState(today).description)
  }
}
