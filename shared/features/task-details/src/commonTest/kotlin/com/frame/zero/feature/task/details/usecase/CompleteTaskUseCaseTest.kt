package com.frame.zero.feature.task.details.usecase

import com.frame.zero.domain.OfflineException
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.task.TaskDetail
import com.frame.zero.domain.task.TaskPriority
import com.frame.zero.domain.task.TaskStatus
import com.frame.zero.testing.FakeTasksRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class CompleteTaskUseCaseTest {
  private val openTask = TaskDetail(
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
  private val doneTask = openTask.copy(status = TaskStatus.DONE)

  @Test
  fun `success returns completed task and forwards id`() =
    runTest {
      val repo = FakeTasksRepository(task = openTask, completedTask = doneTask)

      val outcome = CompleteTaskUseCase(repo)("t1")

      val success = assertIs<Outcome.Success<TaskDetail>>(outcome)
      assertEquals(TaskStatus.DONE, success.data.status)
      assertEquals(listOf("t1"), repo.completeCalls)
    }

  @Test
  fun `OfflineException maps to Offline failure`() =
    runTest {
      val repo = FakeTasksRepository(task = openTask, completeThrows = OfflineException("offline"))

      val outcome = CompleteTaskUseCase(repo)("t1")

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Offline("offline"), failure.error)
    }

  @Test
  fun `generic exception maps to Unknown failure`() =
    runTest {
      val repo = FakeTasksRepository(task = openTask, completeThrows = RuntimeException("boom"))

      val outcome = CompleteTaskUseCase(repo)("t1")

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Unknown("boom"), failure.error)
    }
}
