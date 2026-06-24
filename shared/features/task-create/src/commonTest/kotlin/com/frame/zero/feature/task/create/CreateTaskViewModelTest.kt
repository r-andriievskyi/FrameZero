package com.frame.zero.feature.task.create

import com.frame.zero.core.files.AttachmentFileManager
import com.frame.zero.core.files.FilePicker
import com.frame.zero.core.files.MAX_ATTACHMENT_BYTES
import com.frame.zero.core.files.PickedFile
import com.frame.zero.core.network.connectivity.OfflineException
import com.frame.zero.core.upload.PendingTaskUpload
import com.frame.zero.core.upload.TaskUploadScheduler
import com.frame.zero.feature.task.create.domain.CreateTaskUseCase
import com.frame.zero.feature.task.create.domain.GetAssignableMembersUseCase
import com.frame.zero.testing.FakeProductionsRepository
import com.frame.zero.testing.FakeTasksRepository
import com.frame.zero.testing.productionMemberDto
import com.frame.zero.testing.taskDetailDto
import com.frame.zero.ui.asUiText
import framezero.shared.features.task_create.generated.resources.Res
import framezero.shared.features.task_create.generated.resources.error_network
import framezero.shared.features.task_create.generated.resources.error_title_required
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CreateTaskViewModelTest {
  private fun clockAt(date: LocalDate): Clock =
    object : Clock {
      override fun now(): Instant = date.atStartOfDayIn(TimeZone.UTC)
    }

  @Test
  fun `blank title surfaces title-required error and does not submit`() =
    runTest {
      val tasks = FakeTasksRepository()
      val viewModel = makeViewModel(tasks = tasks)
      advanceUntilIdle()

      viewModel.onIntent(CreateTaskIntent.Submit)
      advanceUntilIdle()

      assertEquals(Res.string.error_title_required.asUiText(), viewModel.state.value.titleError)
      assertTrue(tasks.createRequests.isEmpty())
      assertEquals(false, viewModel.state.value.isLoading)
    }

  @Test
  fun `submit success emits Created event and clears loading`() =
    runTest {
      val tasks = FakeTasksRepository(created = taskDetailDto(id = "t42"))
      val viewModel = makeViewModel(tasks = tasks)
      advanceUntilIdle()
      val events = mutableListOf<CreateTaskEvent>()
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        viewModel.events.collect { events += it }
      }

      viewModel.onIntent(CreateTaskIntent.TitleChanged("Storyboard"))
      viewModel.onIntent(CreateTaskIntent.Submit)
      advanceUntilIdle()

      assertEquals(listOf<CreateTaskEvent>(CreateTaskEvent.Created("t42")), events)
      assertEquals(false, viewModel.state.value.isLoading)
      assertNull(viewModel.state.value.errorToast)
    }

  @Test
  fun `submit failure surfaces a toast and clears loading`() =
    runTest {
      val tasks = FakeTasksRepository(createThrows = OfflineException())
      val viewModel = makeViewModel(tasks = tasks)
      advanceUntilIdle()

      viewModel.onIntent(CreateTaskIntent.TitleChanged("Storyboard"))
      viewModel.onIntent(CreateTaskIntent.Submit)
      advanceUntilIdle()

      assertEquals(Res.string.error_network.asUiText(), viewModel.state.value.errorToast)
      assertEquals(false, viewModel.state.value.isLoading)
    }

  @Test
  fun `dismissing the toast clears it`() =
    runTest {
      val viewModel = makeViewModel(tasks = FakeTasksRepository(createThrows = OfflineException()))
      advanceUntilIdle()
      viewModel.onIntent(CreateTaskIntent.TitleChanged("Storyboard"))
      viewModel.onIntent(CreateTaskIntent.Submit)
      advanceUntilIdle()

      viewModel.onIntent(CreateTaskIntent.ToastDismissed)

      assertNull(viewModel.state.value.errorToast)
    }

  @Test
  fun `loadMembers populates assignable members on success`() =
    runTest {
      val productions = FakeProductionsRepository(
        members = listOf(productionMemberDto(userId = "u1", name = "Ada"))
      )
      val viewModel = makeViewModel(productions = productions)

      advanceUntilIdle()

      assertEquals(listOf("Ada"), viewModel.state.value.assignableMembers.map { it.name })
    }

  @Test
  fun `loadMembers failure leaves the picker empty`() =
    runTest {
      val productions = FakeProductionsRepository(listMembersThrows = OfflineException())
      val viewModel = makeViewModel(productions = productions)

      advanceUntilIdle()

      assertTrue(viewModel.state.value.assignableMembers.isEmpty())
    }

  @Test
  fun `quick due date options resolve against the clock on a Monday`() =
    runTest {
      // 2026-06-22 is a Monday (ISO day 1); end of ISO week is the following Sunday.
      val viewModel = makeViewModel(clock = clockAt(LocalDate(2026, 6, 22)))
      advanceUntilIdle()

      viewModel.onIntent(CreateTaskIntent.QuickDueDateSelected(DueDateQuickOption.TODAY))
      assertEquals(LocalDate(2026, 6, 22), viewModel.state.value.dueDate)

      viewModel.onIntent(CreateTaskIntent.QuickDueDateSelected(DueDateQuickOption.TOMORROW))
      assertEquals(LocalDate(2026, 6, 23), viewModel.state.value.dueDate)

      viewModel.onIntent(CreateTaskIntent.QuickDueDateSelected(DueDateQuickOption.THIS_WEEK))
      assertEquals(LocalDate(2026, 6, 28), viewModel.state.value.dueDate)

      viewModel.onIntent(CreateTaskIntent.QuickDueDateSelected(DueDateQuickOption.NEXT_WEEK))
      assertEquals(LocalDate(2026, 7, 5), viewModel.state.value.dueDate)
    }

  @Test
  fun `this week resolves to today when today is already Sunday`() =
    runTest {
      // 2026-06-28 is a Sunday (ISO day 7) — daysUntilEndOfWeek is 0.
      val viewModel = makeViewModel(clock = clockAt(LocalDate(2026, 6, 28)))
      advanceUntilIdle()

      viewModel.onIntent(CreateTaskIntent.QuickDueDateSelected(DueDateQuickOption.THIS_WEEK))
      assertEquals(LocalDate(2026, 6, 28), viewModel.state.value.dueDate)

      viewModel.onIntent(CreateTaskIntent.QuickDueDateSelected(DueDateQuickOption.NEXT_WEEK))
      assertEquals(LocalDate(2026, 7, 5), viewModel.state.value.dueDate)
    }

  @Test
  fun `attaching an oversize file surfaces an error and discards the copy`() =
    runTest {
      val big = PickedFile("big.bin", MAX_ATTACHMENT_BYTES + 1, "application/octet-stream", "/tmp/big.bin")
      val files = FakeAttachmentFileManager()
      val viewModel = makeViewModel(filePicker = FakeFilePicker(big), attachmentFileManager = files)
      advanceUntilIdle()

      viewModel.onIntent(CreateTaskIntent.AttachFileClicked)
      advanceUntilIdle()

      assertNull(viewModel.state.value.attachment)
      assertNotNull(viewModel.state.value.attachmentError)
      assertEquals(listOf("/tmp/big.bin"), files.deleted)
    }

  @Test
  fun `submitting with an attachment enqueues a background upload`() =
    runTest {
      val file = PickedFile("doc.pdf", 1_024, "application/pdf", "/tmp/doc.pdf")
      val scheduler = FakeTaskUploadScheduler()
      val viewModel = makeViewModel(filePicker = FakeFilePicker(file), uploadScheduler = scheduler)
      advanceUntilIdle()

      viewModel.onIntent(CreateTaskIntent.TitleChanged("Storyboard"))
      viewModel.onIntent(CreateTaskIntent.AttachFileClicked)
      advanceUntilIdle()
      viewModel.onIntent(CreateTaskIntent.Submit)
      advanceUntilIdle()

      val upload = scheduler.enqueued.single()
      assertEquals("p1", upload.productionId)
      assertEquals("Storyboard", upload.title)
      assertEquals("doc.pdf", upload.fileName)
      assertEquals("/tmp/doc.pdf", upload.localPath)
    }

  private fun TestScope.makeViewModel(
    tasks: FakeTasksRepository = FakeTasksRepository(),
    productions: FakeProductionsRepository = FakeProductionsRepository(),
    filePicker: FilePicker = FakeFilePicker(),
    uploadScheduler: FakeTaskUploadScheduler = FakeTaskUploadScheduler(),
    attachmentFileManager: AttachmentFileManager = FakeAttachmentFileManager(),
    clock: Clock = clockAt(LocalDate(2026, 6, 22))
  ): CreateTaskViewModel =
    CreateTaskViewModel(
      productionId = "p1",
      productionTitle = "Pilot",
      createTaskUseCase = CreateTaskUseCase(tasks),
      getAssignableMembersUseCase = GetAssignableMembersUseCase(productions),
      filePicker = filePicker,
      uploadScheduler = uploadScheduler,
      attachmentFileManager = attachmentFileManager,
      clock = clock,
      timeZone = TimeZone.UTC,
      dispatcher = StandardTestDispatcher(testScheduler)
    )

  private class FakeFilePicker(
    private val result: PickedFile? = null
  ) : FilePicker {
    override suspend fun pickFile(): PickedFile? = result
  }

  private class FakeTaskUploadScheduler : TaskUploadScheduler {
    val enqueued: MutableList<PendingTaskUpload> = mutableListOf()

    override suspend fun enqueue(upload: PendingTaskUpload) {
      enqueued += upload
    }

    override suspend fun retry(uploadId: String) = Unit

    override suspend fun cancel(uploadId: String) = Unit
  }

  private class FakeAttachmentFileManager : AttachmentFileManager {
    val deleted: MutableList<String> = mutableListOf()

    override fun cachedAttachment(
      taskId: String,
      fileName: String
    ): String? = null

    override suspend fun saveDownloaded(
      taskId: String,
      fileName: String,
      bytes: ByteArray
    ): String = ""

    override fun readBytes(localPath: String): ByteArray = ByteArray(0)

    override fun delete(localPath: String) {
      deleted += localPath
    }

    override fun openWith(
      localPath: String,
      contentType: String
    ) = Unit

    override fun availableBytes(): Long = Long.MAX_VALUE
  }
}
