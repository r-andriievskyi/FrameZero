package com.frame.zero.feature.task.details.usecase

import com.frame.zero.core.network.connectivity.OfflineException
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.feature.task.details.testing.FakeTasksRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class GetTaskDetailsUseCaseTest {
  private val task = TaskDetailDto(
    id = "t1",
    productionId = "p1",
    productionTitle = "Pilot",
    title = "Storyboard",
    description = null,
    dueDate = null,
    status = TaskStatus.OPEN,
    priority = TaskPriority.MEDIUM,
    assigneeUserId = null,
    assignee = null,
    createdAt = Instant.fromEpochMilliseconds(0)
  )

  @Test
  fun `success returns task and forwards id`() =
    runTest {
      val repo = FakeTasksRepository(task = task)

      val outcome = GetTaskDetailsUseCase(repo)("t1")

      val success = assertIs<Outcome.Success<TaskDetailDto>>(outcome)
      assertEquals(task, success.data)
      assertEquals(listOf("t1"), repo.getCalls)
    }

  @Test
  fun `OfflineException maps to Offline failure`() =
    runTest {
      val repo = FakeTasksRepository(task = task, getThrows = OfflineException("offline"))

      val outcome = GetTaskDetailsUseCase(repo)("t1")

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Offline("offline"), failure.error)
    }

  @Test
  fun `unexpected throwable maps to Unknown failure`() =
    runTest {
      val repo = FakeTasksRepository(task = task, getThrows = RuntimeException("boom"))

      val outcome = GetTaskDetailsUseCase(repo)("t1")

      val failure = assertIs<Outcome.Failure>(outcome)
      assertIs<DomainError.Unknown>(failure.error)
    }
}
