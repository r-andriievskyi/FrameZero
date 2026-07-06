package com.frame.zero.feature.production.details.domain

import com.frame.zero.domain.OfflineException
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.task.TaskStatus
import com.frame.zero.domain.task.TaskSummary
import com.frame.zero.testing.FakeTasksRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetProductionTasksUseCaseTest {
  private fun summary(
    id: String = "t1",
    title: String = "Storyboard",
    dueDate: LocalDate? = LocalDate(2026, 6, 24),
    status: TaskStatus = TaskStatus.OPEN
  ): TaskSummary =
    TaskSummary(
      id = id,
      title = title,
      productionTitle = "Pilot",
      dueDate = dueDate,
      status = status
    )

  @Test
  fun `maps task summaries to production tasks deriving isDone from status`() =
    runTest {
      val repo = FakeTasksRepository(
        tasks = listOf(
          summary(id = "t1", title = "Open one", status = TaskStatus.OPEN),
          summary(id = "t2", title = "Done one", status = TaskStatus.DONE, dueDate = null)
        )
      )

      val outcome = GetProductionTasksUseCase(repo)(GetProductionTasksUseCase.Params("p1"))

      val success = assertIs<Outcome.Success<List<ProductionTask>>>(outcome)
      assertEquals(
        listOf(
          ProductionTask(id = "t1", title = "Open one", dueDate = LocalDate(2026, 6, 24), isDone = false),
          ProductionTask(id = "t2", title = "Done one", dueDate = null, isDone = true)
        ),
        success.data
      )
      assertEquals(listOf("p1"), repo.listedProductionIds)
    }

  @Test
  fun `empty task list maps to an empty result`() =
    runTest {
      val outcome = GetProductionTasksUseCase(FakeTasksRepository())(GetProductionTasksUseCase.Params("p1"))

      val success = assertIs<Outcome.Success<List<ProductionTask>>>(outcome)
      assertEquals(emptyList(), success.data)
    }

  @Test
  fun `failure is mapped to a domain error`() =
    runTest {
      val repo = FakeTasksRepository(listThrows = OfflineException("offline"))

      val outcome = GetProductionTasksUseCase(repo)(GetProductionTasksUseCase.Params("p1"))

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Offline("offline"), failure.error)
    }
}
