package com.frame.zero.feature.task.details

import com.frame.zero.dto.task.TaskAssigneeDto
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.feature.task.details.usecase.CompleteTaskUseCase
import com.frame.zero.feature.task.details.usecase.GetAssignableMembersUseCase
import com.frame.zero.feature.task.details.usecase.GetTaskDetailsUseCase
import com.frame.zero.feature.task.details.usecase.UpdateTaskParticipantsUseCase
import com.frame.zero.testing.FakeProductionsRepository
import com.frame.zero.testing.FakeTasksRepository
import com.frame.zero.testing.productionMemberDto
import com.frame.zero.testing.taskParticipantDto
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
import kotlin.test.assertNotNull
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
      assertEquals("p1", state.productionId)
      assertEquals(TaskPriority.HIGH, state.priority)
      assertEquals(TaskStatus.IN_PROGRESS, state.status)
      assertEquals("MR", state.assignee?.initials)
      assertEquals("Maya Rivera", state.assignee?.name)
      assertTrue(state.showMarkCompleteButton)
      assertEquals(listOf("t1"), repo.getCalls)
    }

  @Test
  fun `exposes the raw due date and flags it as not due today`() =
    runTest {
      val repo = FakeTasksRepository(task = openTask.copy(dueDate = LocalDate(2026, 4, 6)))
      val viewModel = makeViewModel(this, repo)

      advanceUntilIdle()

      val state = viewModel.state.value
      assertEquals(LocalDate(2026, 4, 6), state.dueDate)
      assertFalse(state.isDueToday)
    }

  @Test
  fun `null due date and assignee map to null and blank description maps to empty string`() =
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

        override suspend fun createTask(request: com.frame.zero.dto.task.CreateTaskRequest): TaskDetailDto = openTask

        override suspend fun updateParticipants(
          taskId: String,
          userIds: List<String>
        ): TaskDetailDto = openTask

        override suspend fun listForProduction(productionId: String): List<com.frame.zero.dto.task.TaskSummaryDto> =
          emptyList()

        override suspend fun downloadAttachment(
          taskId: String,
          fileName: String,
          expectedBytes: Long
        ): com.frame.zero.domain.Outcome<String> = com.frame.zero.domain.Outcome.Success("/local")
      }
      val viewModel =
        TaskDetailsViewModel(
          taskId = "t1",
          getTaskDetailsUseCase = GetTaskDetailsUseCase(repo),
          completeTaskUseCase = CompleteTaskUseCase(repo),
          getAssignableMembersUseCase = GetAssignableMembersUseCase(FakeProductionsRepository()),
          updateTaskParticipantsUseCase = UpdateTaskParticipantsUseCase(repo),
          tasksRepository = repo,
          attachmentFileManager = FakeAttachmentFileManager(),
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

        override suspend fun createTask(request: com.frame.zero.dto.task.CreateTaskRequest): TaskDetailDto = openTask

        override suspend fun updateParticipants(
          taskId: String,
          userIds: List<String>
        ): TaskDetailDto = openTask

        override suspend fun listForProduction(productionId: String): List<com.frame.zero.dto.task.TaskSummaryDto> =
          emptyList()

        override suspend fun downloadAttachment(
          taskId: String,
          fileName: String,
          expectedBytes: Long
        ): com.frame.zero.domain.Outcome<String> = com.frame.zero.domain.Outcome.Success("/local")
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

  @Test
  fun `loads assignable members for the task's production after load succeeds`() =
    runTest {
      val productions = FakeProductionsRepository(
        members = listOf(productionMemberDto(userId = "u2", name = "Jake"))
      )
      val viewModel = makeViewModel(this, FakeTasksRepository(task = openTask), productions = productions)

      advanceUntilIdle()

      assertEquals(listOf("p1"), productions.listMembersCalls)
      assertEquals(listOf("Jake"), viewModel.state.value.assignableMembers.map { it.name })
    }

  @Test
  fun `initial participants come from the loaded task detail`() =
    runTest {
      val task = openTask.copy(participants = listOf(taskParticipantDto(userId = "u2", name = "Jake")))
      val viewModel = makeViewModel(this, FakeTasksRepository(task = task))

      advanceUntilIdle()

      assertEquals(listOf("Jake"), viewModel.state.value.participants.map { it.name })
    }

  @Test
  fun `toggling an unselected participant adds them and calls the repository with the full set`() =
    runTest {
      val task = openTask.copy(participants = listOf(taskParticipantDto(userId = "u2", name = "Jake")))
      val updated = task.copy(
        participants = listOf(
          taskParticipantDto(userId = "u2", name = "Jake"),
          taskParticipantDto(userId = "u3", name = "Mia")
        )
      )
      val repo = FakeTasksRepository(task = task, updatedParticipantsTask = updated)
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()

      viewModel.onIntent(TaskDetailsIntent.ParticipantToggled("u3"))
      advanceUntilIdle()

      assertEquals(listOf("t1" to listOf("u2", "u3")), repo.updateParticipantsCalls)
      assertEquals(listOf("Jake", "Mia"), viewModel.state.value.participants.map { it.name })
      assertFalse(viewModel.state.value.isUpdatingParticipants)
    }

  @Test
  fun `toggling a selected participant removes them`() =
    runTest {
      val task = openTask.copy(participants = listOf(taskParticipantDto(userId = "u2", name = "Jake")))
      val updated = task.copy(participants = emptyList())
      val repo = FakeTasksRepository(task = task, updatedParticipantsTask = updated)
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()

      viewModel.onIntent(TaskDetailsIntent.ParticipantToggled("u2"))
      advanceUntilIdle()

      assertEquals(listOf("t1" to emptyList<String>()), repo.updateParticipantsCalls)
      assertTrue(viewModel.state.value.participants.isEmpty())
    }

  @Test
  fun `a failed participant update surfaces an error and keeps the previous list`() =
    runTest {
      val task = openTask.copy(participants = listOf(taskParticipantDto(userId = "u2", name = "Jake")))
      val repo = FakeTasksRepository(
        task = task,
        updateParticipantsThrows = com.frame.zero.core.network.connectivity.OfflineException()
      )
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()

      viewModel.onIntent(TaskDetailsIntent.ParticipantToggled("u3"))
      advanceUntilIdle()

      assertNotNull(viewModel.state.value.participantsError)
      assertEquals(listOf("Jake"), viewModel.state.value.participants.map { it.name })
      assertFalse(viewModel.state.value.isUpdatingParticipants)
    }

  @Test
  fun `dismissing the participants error clears it`() =
    runTest {
      val repo = FakeTasksRepository(
        task = openTask,
        updateParticipantsThrows = com.frame.zero.core.network.connectivity.OfflineException()
      )
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()
      viewModel.onIntent(TaskDetailsIntent.ParticipantToggled("u3"))
      advanceUntilIdle()
      assertNotNull(viewModel.state.value.participantsError)

      viewModel.onIntent(TaskDetailsIntent.ParticipantsErrorDismissed)

      assertNull(viewModel.state.value.participantsError)
    }

  @Test
  fun `participant picker open dismiss and search update state`() =
    runTest {
      val viewModel = makeViewModel(this, FakeTasksRepository(task = openTask))
      advanceUntilIdle()

      viewModel.onIntent(TaskDetailsIntent.ParticipantPickerOpened)
      assertTrue(viewModel.state.value.isParticipantPickerVisible)

      viewModel.onIntent(TaskDetailsIntent.ParticipantSearchChanged("ja"))
      assertEquals("ja", viewModel.state.value.participantQuery)

      viewModel.onIntent(TaskDetailsIntent.ParticipantPickerDismissed)
      assertFalse(viewModel.state.value.isParticipantPickerVisible)
      assertEquals("", viewModel.state.value.participantQuery)
    }

  private fun makeViewModel(
    scope: TestScope,
    repo: com.frame.zero.repository.tasks.TasksRepository,
    productions: FakeProductionsRepository = FakeProductionsRepository(),
    attachmentFileManager: com.frame.zero.core.files.AttachmentFileManager = FakeAttachmentFileManager()
  ): TaskDetailsViewModel =
    TaskDetailsViewModel(
      taskId = "t1",
      getTaskDetailsUseCase = GetTaskDetailsUseCase(repo),
      completeTaskUseCase = CompleteTaskUseCase(repo),
      getAssignableMembersUseCase = GetAssignableMembersUseCase(productions),
      updateTaskParticipantsUseCase = UpdateTaskParticipantsUseCase(repo),
      tasksRepository = repo,
      attachmentFileManager = attachmentFileManager,
      dispatcher = StandardTestDispatcher(scope.testScheduler)
    )

  private class FakeAttachmentFileManager : com.frame.zero.core.files.AttachmentFileManager {
    val opened: MutableList<String> = mutableListOf()

    override fun cachedAttachment(
      taskId: String,
      fileName: String
    ): String? = null

    override suspend fun saveDownloaded(
      taskId: String,
      fileName: String,
      channel: io.ktor.utils.io.ByteReadChannel
    ): String = ""

    override fun delete(localPath: String) = Unit

    override fun openWith(
      localPath: String,
      contentType: String
    ) {
      opened += localPath
    }

    override fun availableBytes(): Long = Long.MAX_VALUE
  }
}
