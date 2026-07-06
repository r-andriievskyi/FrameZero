package com.frame.zero.feature.task.create.domain

import com.frame.zero.domain.OfflineException
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.task.TaskDetail
import com.frame.zero.domain.task.TaskPriority
import com.frame.zero.testing.FakeTasksRepository
import com.frame.zero.testing.taskDetail
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class CreateTaskUseCaseTest {
  private fun params(
    title: String = "Storyboard",
    description: String? = "Draft the opening scene",
    dueDate: LocalDate? = LocalDate(2026, 6, 24),
    assigneeUserId: String? = "u1",
    priority: TaskPriority = TaskPriority.HIGH,
    participantUserIds: List<String> = listOf("u2", "u3")
  ): CreateTaskUseCase.Params =
    CreateTaskUseCase.Params(
      productionId = "p1",
      title = title,
      description = description,
      dueDate = dueDate,
      assigneeUserId = assigneeUserId,
      priority = priority,
      participantUserIds = participantUserIds
    )

  @Test
  fun `success forwards request fields and returns the created task`() =
    runTest {
      val repo = FakeTasksRepository(created = taskDetail(id = "t9", title = "Storyboard"))

      val outcome = CreateTaskUseCase(repo)(params())

      val success = assertIs<Outcome.Success<TaskDetail>>(outcome)
      assertEquals("t9", success.data.id)
      val request = repo.createRequests.single()
      assertEquals("p1", request.productionId)
      assertEquals("Storyboard", request.title)
      assertEquals("Draft the opening scene", request.description)
      assertEquals(LocalDate(2026, 6, 24), request.dueDate)
      assertEquals("u1", request.assigneeUserId)
      assertEquals(TaskPriority.HIGH, request.priority)
      assertEquals(listOf("u2", "u3"), request.participantUserIds)
    }

  @Test
  fun `title and description are trimmed`() =
    runTest {
      val repo = FakeTasksRepository()

      CreateTaskUseCase(repo)(params(title = "  Storyboard  ", description = "  notes  "))

      val request = repo.createRequests.single()
      assertEquals("Storyboard", request.title)
      assertEquals("notes", request.description)
    }

  @Test
  fun `blank description becomes null`() =
    runTest {
      val repo = FakeTasksRepository()

      CreateTaskUseCase(repo)(params(description = "   "))

      assertNull(repo.createRequests.single().description)
    }

  @Test
  fun `OfflineException maps to Offline failure`() =
    runTest {
      val repo = FakeTasksRepository(createThrows = OfflineException("offline"))

      val outcome = CreateTaskUseCase(repo)(params())

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Offline("offline"), failure.error)
    }
}
