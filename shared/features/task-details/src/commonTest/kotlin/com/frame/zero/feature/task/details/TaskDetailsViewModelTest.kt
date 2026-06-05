package com.frame.zero.feature.task.details

import com.frame.zero.dto.task.TaskAssigneeDto
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.feature.task.details.testing.FakeTasksRepository
import com.frame.zero.feature.task.details.usecase.CompleteTaskUseCase
import com.frame.zero.feature.task.details.usecase.GetTaskDetailsUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import com.frame.zero.dto.task.TaskStatus as DtoTaskStatus

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class TaskDetailsViewModelTest {
  private val openTask = TaskDetailDto(
    id = "t1",
    productionId = "p1",
    productionTitle = "Echoes of Silence",
    title = "Review Scene 12",
    description = "Check the revised pages.",
    dueDate = null,
    status = DtoTaskStatus.OPEN,
    priority = com.frame.zero.dto.task.TaskPriority.HIGH,
    assigneeUserId = "u1",
    assignee = TaskAssigneeDto(userId = "u1", name = "Maya Rivera", avatarColorHex = "#0097A7"),
    createdAt = Instant.fromEpochMilliseconds(0L)
  )

  @Test
  fun `init loads task and maps fields then clears loading`() =
    runTest {
      val repo = FakeTasksRepository(task = openTask)
      val viewModel = makeViewModel(this, repo)

      advanceUntilIdle()

      val state = viewModel.state.value
      assertFalse(state.isLoading)
      assertFalse(state.isError)
      assertEquals("t1", state.taskId)
      assertEquals("Review Scene 12", state.title)
      assertEquals("Echoes of Silence", state.productionName)
      assertEquals(TaskPriority.HIGH, state.priority)
      assertEquals(TaskStatus.IN_PROGRESS, state.status)
      assertEquals("MR", state.assignee?.initials)
      assertEquals("Maya Rivera", state.assignee?.name)
      assertTrue(state.showMarkCompleteButton)
      assertEquals(listOf("t1"), repo.getCalls)
    }

  @Test
  fun `formats due date as abbreviated month day year and not due today`() =
    runTest {
      val repo = FakeTasksRepository(task = openTask.copy(dueDate = LocalDate(2026, 4, 6)))
      val viewModel = makeViewModel(this, repo)

      advanceUntilIdle()

      val state = viewModel.state.value
      assertEquals("Apr 6, 2026", state.dueDate)
      assertFalse(state.isDueToday)
    }

  @Test
  fun `null due date and assignee map to null, blank description maps to empty string`() =
    runTest {
      val repo = FakeTasksRepository(
        task = openTask.copy(dueDate = null, assignee = null, description = null)
      )
      val viewModel = makeViewModel(this, repo)

      advanceUntilIdle()

      val state = viewModel.state.value
      assertNull(state.dueDate)
      assertNull(state.assignee)
      assertEquals("", state.description)
    }

  @Test
  fun `init sets loading true before completion`() =
    runTest {
      val gate = CompletableDeferred<TaskDetailDto>()
      val repo = object : com.frame.zero.repository.tasks.TasksRepository {
        override suspend fun getTask(id: String): TaskDetailDto = gate.await()

        override suspend fun completeTask(id: String): TaskDetailDto = openTask
      }
      val viewModel =
        TaskDetailsViewModel(
          taskId = "t1",
          getTaskDetailsUseCase = GetTaskDetailsUseCase(repo),
          completeTaskUseCase = CompleteTaskUseCase(repo),
          dispatcher = StandardTestDispatcher(testScheduler)
        )

      runCurrent()
      assertTrue(viewModel.state.value.isLoading)

      gate.complete(openTask)
      advanceUntilIdle()
      assertFalse(viewModel.state.value.isLoading)
    }

  @Test
  fun `load failure sets isError true`() =
    runTest {
      val repo = FakeTasksRepository(task = openTask, getThrows = RuntimeException("boom"))
      val viewModel = makeViewModel(this, repo)

      advanceUntilIdle()

      assertTrue(viewModel.state.value.isError)
      assertFalse(viewModel.state.value.isLoading)
    }

  @Test
  fun `done task maps to completed and hides button`() =
    runTest {
      val repo = FakeTasksRepository(task = openTask.copy(status = DtoTaskStatus.DONE))
      val viewModel = makeViewModel(this, repo)

      advanceUntilIdle()

      assertEquals(TaskStatus.COMPLETED, viewModel.state.value.status)
      assertFalse(viewModel.state.value.showMarkCompleteButton)
    }

  @Test
  fun `markComplete sets status completed and hides button`() =
    runTest {
      val repo = FakeTasksRepository(
        task = openTask,
        completedTask = openTask.copy(status = DtoTaskStatus.DONE)
      )
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()

      viewModel.onIntent(TaskDetailsIntent.MarkComplete)
      advanceUntilIdle()

      assertEquals(TaskStatus.COMPLETED, viewModel.state.value.status)
      assertFalse(viewModel.state.value.showMarkCompleteButton)
      assertEquals(listOf("t1"), repo.completeCalls)
    }

  @Test
  fun `refresh after failure reloads task`() =
    runTest {
      var shouldFail = true
      val repo = object : com.frame.zero.repository.tasks.TasksRepository {
        var calls = 0

        override suspend fun getTask(id: String): TaskDetailDto {
          calls++
          if (shouldFail) throw RuntimeException("boom")
          return openTask
        }

        override suspend fun completeTask(id: String): TaskDetailDto = openTask
      }
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()
      assertTrue(viewModel.state.value.isError)

      shouldFail = false
      viewModel.onIntent(TaskDetailsIntent.Refresh)
      advanceUntilIdle()

      assertFalse(viewModel.state.value.isError)
      assertEquals("Review Scene 12", viewModel.state.value.title)
      assertEquals(2, repo.calls)
    }

  private fun makeViewModel(
    scope: TestScope,
    repo: com.frame.zero.repository.tasks.TasksRepository
  ): TaskDetailsViewModel =
    TaskDetailsViewModel(
      taskId = "t1",
      getTaskDetailsUseCase = GetTaskDetailsUseCase(repo),
      completeTaskUseCase = CompleteTaskUseCase(repo),
      dispatcher = StandardTestDispatcher(scope.testScheduler)
    )
}
