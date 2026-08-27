package com.frame.zero.feature.task.create

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.frame.zero.core.files.AttachmentFileManager
import io.ktor.utils.io.ByteReadChannel
import com.frame.zero.core.files.FilePicker
import com.frame.zero.core.files.MAX_ATTACHMENT_BYTES
import com.frame.zero.core.files.PickedFile
import com.frame.zero.domain.OfflineException
import com.frame.zero.feature.task.create.domain.CreateTaskUseCase
import com.frame.zero.feature.task.create.domain.GetAssignableMembersUseCase
import com.frame.zero.testing.FakeProductionsRepository
import com.frame.zero.testing.FakeTaskUploadScheduler
import com.frame.zero.testing.FakeTasksRepository
import com.frame.zero.testing.productionMember
import com.frame.zero.testing.taskDetail
import com.frame.zero.ui.asUiText
import framezero.shared.features.task_create.generated.resources.Res
import framezero.shared.features.task_create.generated.resources.error_title_required
import framezero.shared.ui_text.generated.resources.Res as UiTextRes
import framezero.shared.ui_text.generated.resources.error_network
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
      viewModel.state.test {
        viewModel.onIntent(CreateTaskIntent.Submit)
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertEquals(Res.string.error_title_required.asUiText(), state.titleError)
        assertEquals(false, state.isLoading)
      }
      assertTrue(tasks.createRequests.isEmpty())
    }

  @Test
  fun `submit success emits Created event and clears loading`() =
    runTest {
      val tasks = FakeTasksRepository(created = taskDetail(id = "t42"))
      val viewModel = makeViewModel(tasks = tasks)
      advanceUntilIdle()

      turbineScope {
        val state = viewModel.state.testIn(backgroundScope)
        val events = viewModel.events.testIn(backgroundScope)

        viewModel.onIntent(CreateTaskIntent.TitleChanged("Storyboard"))
        viewModel.onIntent(CreateTaskIntent.Submit)
        advanceUntilIdle()

        assertEquals(CreateTaskEvent.Created("t42"), events.awaitItem())
        val settled = state.expectMostRecentItem()
        assertEquals(false, settled.isLoading)
        assertNull(settled.errorToast)

        state.cancelAndIgnoreRemainingEvents()
        events.cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `submit failure surfaces a toast and clears loading`() =
    runTest {
      val tasks = FakeTasksRepository(createThrows = OfflineException())
      val viewModel = makeViewModel(tasks = tasks)
      viewModel.state.test {
        viewModel.onIntent(CreateTaskIntent.TitleChanged("Storyboard"))
        viewModel.onIntent(CreateTaskIntent.Submit)
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertEquals(UiTextRes.string.error_network.asUiText(), state.errorToast)
        assertEquals(false, state.isLoading)
      }
    }

  @Test
  fun `dismissing the toast clears it`() =
    runTest {
      val viewModel = makeViewModel(tasks = FakeTasksRepository(createThrows = OfflineException()))
      advanceUntilIdle()
      viewModel.onIntent(CreateTaskIntent.TitleChanged("Storyboard"))
      viewModel.onIntent(CreateTaskIntent.Submit)
      advanceUntilIdle()

      viewModel.state.test {
        viewModel.onIntent(CreateTaskIntent.ToastDismissed)
        runCurrent()

        assertNull(expectMostRecentItem().errorToast)
      }
    }

  @Test
  fun `loadMembers populates assignable members on success`() =
    runTest {
      val productions = FakeProductionsRepository(
        members = listOf(productionMember(userId = "u1", name = "Ada"))
      )
      val viewModel = makeViewModel(productions = productions)

      viewModel.state.test {
        advanceUntilIdle()

        assertEquals(listOf("Ada"), expectMostRecentItem().assignableMembers.map { it.name })
      }
    }

  @Test
  fun `loadMembers failure leaves the picker empty`() =
    runTest {
      val productions = FakeProductionsRepository(listMembersThrows = OfflineException())
      val viewModel = makeViewModel(productions = productions)

      viewModel.state.test {
        advanceUntilIdle()

        assertTrue(expectMostRecentItem().assignableMembers.isEmpty())
      }
    }

  @Test
  fun `participant picker open dismiss and search update state`() =
    runTest {
      val viewModel = makeViewModel()
      advanceUntilIdle()

      viewModel.state.test {
        viewModel.onIntent(CreateTaskIntent.ParticipantPickerOpened)
        runCurrent()
        assertTrue(expectMostRecentItem().isParticipantPickerVisible)

        viewModel.onIntent(CreateTaskIntent.ParticipantSearchChanged("ja"))
        runCurrent()
        assertEquals("ja", expectMostRecentItem().participantQuery)

        viewModel.onIntent(CreateTaskIntent.ParticipantPickerDismissed)
        runCurrent()
        val state = expectMostRecentItem()
        assertFalse(state.isParticipantPickerVisible)
        assertEquals("", state.participantQuery)
      }
    }

  @Test
  fun `toggling a participant twice selects then deselects them`() =
    runTest {
      val viewModel = makeViewModel()
      advanceUntilIdle()

      viewModel.state.test {
        viewModel.onIntent(CreateTaskIntent.ParticipantToggled("u1"))
        runCurrent()
        assertEquals(listOf("u1"), expectMostRecentItem().participantUserIds)

        viewModel.onIntent(CreateTaskIntent.ParticipantToggled("u2"))
        runCurrent()
        assertEquals(listOf("u1", "u2"), expectMostRecentItem().participantUserIds)

        viewModel.onIntent(CreateTaskIntent.ParticipantToggled("u1"))
        runCurrent()
        assertEquals(listOf("u2"), expectMostRecentItem().participantUserIds)
      }
    }

  @Test
  fun `selected participants are reflected as a filtered member subset`() =
    runTest {
      val productions = FakeProductionsRepository(
        members = listOf(
          productionMember(userId = "u1", name = "Ada"),
          productionMember(userId = "u2", name = "Jake")
        )
      )
      val viewModel = makeViewModel(productions = productions)
      advanceUntilIdle()

      viewModel.state.test {
        viewModel.onIntent(CreateTaskIntent.ParticipantToggled("u2"))
        runCurrent()

        assertEquals(listOf("Jake"), expectMostRecentItem().selectedParticipants.map { it.name })
      }
    }

  @Test
  fun `submit forwards selected participant ids to the create request`() =
    runTest {
      val tasks = FakeTasksRepository()
      val viewModel = makeViewModel(tasks = tasks)
      advanceUntilIdle()

      viewModel.onIntent(CreateTaskIntent.TitleChanged("Storyboard"))
      viewModel.onIntent(CreateTaskIntent.ParticipantToggled("u1"))
      viewModel.onIntent(CreateTaskIntent.ParticipantToggled("u2"))
      viewModel.onIntent(CreateTaskIntent.Submit)
      advanceUntilIdle()

      assertEquals(listOf("u1", "u2"), tasks.createRequests.single().participantUserIds)
    }

  @Test
  fun `submitting with an attachment carries selected participants into the pending upload`() =
    runTest {
      val file = PickedFile("doc.pdf", 1_024, "application/pdf", "/tmp/doc.pdf")
      val scheduler = FakeTaskUploadScheduler()
      val viewModel = makeViewModel(filePicker = FakeFilePicker(file), uploadScheduler = scheduler)
      advanceUntilIdle()

      viewModel.onIntent(CreateTaskIntent.TitleChanged("Storyboard"))
      viewModel.onIntent(CreateTaskIntent.ParticipantToggled("u1"))
      viewModel.onIntent(CreateTaskIntent.AttachFileClicked)
      advanceUntilIdle()
      viewModel.onIntent(CreateTaskIntent.Submit)
      advanceUntilIdle()

      assertEquals(listOf("u1"), scheduler.enqueued.single().participantUserIds)
    }

  @Test
  fun `quick due date options resolve against the clock on a Monday`() =
    runTest {
      // 2026-06-22 is a Monday (ISO day 1); end of ISO week is the following Sunday.
      val viewModel = makeViewModel(clock = clockAt(LocalDate(2026, 6, 22)))
      advanceUntilIdle()

      viewModel.state.test {
        viewModel.onIntent(CreateTaskIntent.QuickDueDateSelected(DueDateQuickOption.TODAY))
        runCurrent()
        assertEquals(LocalDate(2026, 6, 22), expectMostRecentItem().dueDate)

        viewModel.onIntent(CreateTaskIntent.QuickDueDateSelected(DueDateQuickOption.TOMORROW))
        runCurrent()
        assertEquals(LocalDate(2026, 6, 23), expectMostRecentItem().dueDate)

        viewModel.onIntent(CreateTaskIntent.QuickDueDateSelected(DueDateQuickOption.THIS_WEEK))
        runCurrent()
        assertEquals(LocalDate(2026, 6, 28), expectMostRecentItem().dueDate)

        viewModel.onIntent(CreateTaskIntent.QuickDueDateSelected(DueDateQuickOption.NEXT_WEEK))
        runCurrent()
        assertEquals(LocalDate(2026, 7, 5), expectMostRecentItem().dueDate)
      }
    }

  @Test
  fun `this week resolves to today when today is already Sunday`() =
    runTest {
      // 2026-06-28 is a Sunday (ISO day 7) — daysUntilEndOfWeek is 0.
      val viewModel = makeViewModel(clock = clockAt(LocalDate(2026, 6, 28)))
      advanceUntilIdle()

      viewModel.state.test {
        viewModel.onIntent(CreateTaskIntent.QuickDueDateSelected(DueDateQuickOption.THIS_WEEK))
        runCurrent()
        assertEquals(LocalDate(2026, 6, 28), expectMostRecentItem().dueDate)

        viewModel.onIntent(CreateTaskIntent.QuickDueDateSelected(DueDateQuickOption.NEXT_WEEK))
        runCurrent()
        assertEquals(LocalDate(2026, 7, 5), expectMostRecentItem().dueDate)
      }
    }

  @Test
  fun `attaching an oversize file surfaces an error and discards the copy`() =
    runTest {
      val big = PickedFile("big.bin", MAX_ATTACHMENT_BYTES + 1, "application/octet-stream", "/tmp/big.bin")
      val files = FakeAttachmentFileManager()
      val viewModel = makeViewModel(filePicker = FakeFilePicker(big), attachmentFileManager = files)
      viewModel.state.test {
        viewModel.onIntent(CreateTaskIntent.AttachFileClicked)
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertNull(state.attachment)
        assertNotNull(state.attachmentError)
      }
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

  private class FakeAttachmentFileManager : AttachmentFileManager {
    val deleted: MutableList<String> = mutableListOf()

    override fun cachedAttachment(
      taskId: String,
      fileName: String
    ): String? = null

    override suspend fun saveDownloaded(
      taskId: String,
      fileName: String,
      channel: ByteReadChannel
    ): String = ""

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
