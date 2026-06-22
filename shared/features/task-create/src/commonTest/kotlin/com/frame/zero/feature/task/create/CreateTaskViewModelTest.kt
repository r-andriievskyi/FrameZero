package com.frame.zero.feature.task.create

import com.frame.zero.core.network.connectivity.OfflineException
import com.frame.zero.feature.task.create.domain.CreateTaskUseCase
import com.frame.zero.feature.task.create.domain.GetAssignableMembersUseCase
import com.frame.zero.feature.task.create.testing.FakeProductionsRepository
import com.frame.zero.feature.task.create.testing.FakeTasksRepository
import com.frame.zero.feature.task.create.testing.productionMemberDto
import com.frame.zero.feature.task.create.testing.taskDetailDto
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

  private fun TestScope.makeViewModel(
    tasks: FakeTasksRepository = FakeTasksRepository(),
    productions: FakeProductionsRepository = FakeProductionsRepository(),
    clock: Clock = clockAt(LocalDate(2026, 6, 22))
  ): CreateTaskViewModel =
    CreateTaskViewModel(
      productionId = "p1",
      productionTitle = "Pilot",
      createTaskUseCase = CreateTaskUseCase(tasks),
      getAssignableMembersUseCase = GetAssignableMembersUseCase(productions),
      clock = clock,
      timeZone = TimeZone.UTC,
      dispatcher = StandardTestDispatcher(testScheduler)
    )
}
