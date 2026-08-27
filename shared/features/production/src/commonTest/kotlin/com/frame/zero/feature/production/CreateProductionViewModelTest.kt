package com.frame.zero.feature.production

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.frame.zero.domain.OfflineException
import com.frame.zero.feature.production.domain.CreateProductionUseCase
import com.frame.zero.testing.FakeProductionsRepository
import com.frame.zero.ui.asUiText
import framezero.shared.features.production.generated.resources.Res
import framezero.shared.features.production.generated.resources.error_invalid_dates
import framezero.shared.features.production.generated.resources.error_missing_dates
import framezero.shared.features.production.generated.resources.error_title_required
import framezero.shared.ui_text.generated.resources.Res as UiTextRes
import framezero.shared.ui_text.generated.resources.error_network
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CreateProductionViewModelTest {
  private val start = LocalDate(2026, 4, 1)
  private val wrap = LocalDate(2026, 5, 1)

  @Test
  fun `step 1 can advance once title and valid dates are set`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())

      viewModel.state.test {
        viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))
        viewModel.onIntent(CreateProductionIntent.StartDateChanged(start))
        viewModel.onIntent(CreateProductionIntent.WrapDateChanged(wrap))
        runCurrent()

        assertTrue(expectMostRecentItem().canAdvanceStep1)
      }
    }

  @Test
  fun `step 1 cannot advance when wrap date is not after start date`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())

      viewModel.state.test {
        viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))
        viewModel.onIntent(CreateProductionIntent.StartDateChanged(start))
        viewModel.onIntent(CreateProductionIntent.WrapDateChanged(start))
        runCurrent()

        assertEquals(false, expectMostRecentItem().canAdvanceStep1)
      }
    }

  @Test
  fun `next step on blank title surfaces title-required error`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())

      viewModel.state.test {
        viewModel.onIntent(CreateProductionIntent.NextStep)
        runCurrent()

        val state = expectMostRecentItem()
        assertEquals(Res.string.error_title_required.asUiText(), state.error)
        assertEquals(1, state.currentStep)
      }
    }

  @Test
  fun `next step with title but invalid dates surfaces invalid-dates error`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())

      viewModel.state.test {
        viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))
        viewModel.onIntent(CreateProductionIntent.NextStep)
        runCurrent()

        assertEquals(Res.string.error_invalid_dates.asUiText(), expectMostRecentItem().error)
      }
    }

  @Test
  fun `next then previous step navigates between steps`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))
      viewModel.onIntent(CreateProductionIntent.StartDateChanged(start))
      viewModel.onIntent(CreateProductionIntent.WrapDateChanged(wrap))

      viewModel.state.test {
        viewModel.onIntent(CreateProductionIntent.NextStep)
        runCurrent()
        assertEquals(2, expectMostRecentItem().currentStep)

        viewModel.onIntent(CreateProductionIntent.PreviousStep)
        runCurrent()
        assertEquals(1, expectMostRecentItem().currentStep)
      }
    }

  @Test
  fun `back press past step 1 steps the wizard back without emitting`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))
      viewModel.onIntent(CreateProductionIntent.StartDateChanged(start))
      viewModel.onIntent(CreateProductionIntent.WrapDateChanged(wrap))
      viewModel.onIntent(CreateProductionIntent.NextStep)

      turbineScope {
        val state = viewModel.state.testIn(backgroundScope)
        val events = viewModel.events.testIn(backgroundScope)

        viewModel.onIntent(CreateProductionIntent.BackPressed)
        advanceUntilIdle()

        assertEquals(1, state.expectMostRecentItem().currentStep)
        events.expectNoEvents()

        state.cancelAndIgnoreRemainingEvents()
        events.cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `back press on step 1 emits Dismissed`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())

      turbineScope {
        val state = viewModel.state.testIn(backgroundScope)
        val events = viewModel.events.testIn(backgroundScope)

        viewModel.onIntent(CreateProductionIntent.BackPressed)
        advanceUntilIdle()

        assertEquals(1, state.expectMostRecentItem().currentStep)
        assertEquals(CreateProductionEvent.Dismissed, events.awaitItem())

        state.cancelAndIgnoreRemainingEvents()
        events.cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `budget changed formats display with thousands separators`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())

      viewModel.state.test {
        // Exact symbol/grouping/sign glyph is locale-dependent by design
        // (formatCurrencyUsdCents is expect/actual) — strip everything but digits so the
        // assertion holds regardless of separator character or symbol placement.
        viewModel.onIntent(CreateProductionIntent.BudgetChanged(123_456))
        runCurrent()
        val positiveDisplay = expectMostRecentItem().budgetDisplay.orEmpty()
        assertEquals("1234", positiveDisplay.filter { it.isDigit() })

        viewModel.onIntent(CreateProductionIntent.BudgetChanged(-123_456))
        runCurrent()
        val negativeDisplay = expectMostRecentItem().budgetDisplay.orEmpty()
        assertEquals("1234", negativeDisplay.filter { it.isDigit() })
        assertTrue(negativeDisplay != positiveDisplay)
      }
    }

  @Test
  fun `add crew member trims name and resets inputs`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      viewModel.onIntent(CreateProductionIntent.CrewNameChanged("  Ada  "))
      viewModel.onIntent(CreateProductionIntent.CrewRoleChanged("Producer"))

      viewModel.state.test {
        viewModel.onIntent(CreateProductionIntent.AddCrewMember)
        runCurrent()

        val state = expectMostRecentItem()
        val crew = state.crewMembers
        assertEquals(1, crew.size)
        assertEquals("Ada", crew.single().name)
        assertEquals("Producer", crew.single().role)
        assertEquals("", state.crewNameInput)
        assertEquals(DEFAULT_CREW_ROLE, state.crewRoleInput)
      }
    }

  @Test
  fun `add crew member ignores blank name`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      viewModel.onIntent(CreateProductionIntent.CrewNameChanged("   "))

      viewModel.state.test {
        viewModel.onIntent(CreateProductionIntent.AddCrewMember)
        runCurrent()

        assertTrue(expectMostRecentItem().crewMembers.isEmpty())
      }
    }

  @Test
  fun `remove crew member drops the entry at the index`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      viewModel.onIntent(CreateProductionIntent.CrewNameChanged("Ada"))
      viewModel.onIntent(CreateProductionIntent.AddCrewMember)
      viewModel.onIntent(CreateProductionIntent.CrewNameChanged("Bo"))
      viewModel.onIntent(CreateProductionIntent.AddCrewMember)

      viewModel.state.test {
        viewModel.onIntent(CreateProductionIntent.RemoveCrewMember(0))
        runCurrent()

        assertEquals(listOf("Bo"), expectMostRecentItem().crewMembers.map { it.name })
      }
    }

  @Test
  fun `submit without dates surfaces missing-dates error`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))

      viewModel.state.test {
        viewModel.onIntent(CreateProductionIntent.Submit)
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertEquals(Res.string.error_missing_dates.asUiText(), state.error)
        assertEquals(false, state.isLoading)
      }
    }

  @Test
  fun `submit success emits navigation event`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))
      viewModel.onIntent(CreateProductionIntent.StartDateChanged(start))
      viewModel.onIntent(CreateProductionIntent.WrapDateChanged(wrap))

      turbineScope {
        val state = viewModel.state.testIn(backgroundScope)
        val events = viewModel.events.testIn(backgroundScope)

        viewModel.onIntent(CreateProductionIntent.Submit)
        advanceUntilIdle()

        assertEquals(CreateProductionEvent.Created, events.awaitItem())
        val settled = state.expectMostRecentItem()
        assertNull(settled.error)
        assertEquals(false, settled.isLoading)

        state.cancelAndIgnoreRemainingEvents()
        events.cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `submit network failure surfaces a toast not an inline error`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository(createThrows = OfflineException()))
      viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))
      viewModel.onIntent(CreateProductionIntent.StartDateChanged(start))
      viewModel.onIntent(CreateProductionIntent.WrapDateChanged(wrap))

      viewModel.state.test {
        viewModel.onIntent(CreateProductionIntent.Submit)
        advanceUntilIdle()

        val state = expectMostRecentItem()
        assertEquals(UiTextRes.string.error_network.asUiText(), state.errorToast)
        assertNull(state.error)
        assertEquals(false, state.isLoading)
      }
    }

  @Test
  fun `dismissing the toast clears it`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository(createThrows = OfflineException()))
      viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))
      viewModel.onIntent(CreateProductionIntent.StartDateChanged(start))
      viewModel.onIntent(CreateProductionIntent.WrapDateChanged(wrap))
      viewModel.onIntent(CreateProductionIntent.Submit)
      advanceUntilIdle()

      viewModel.state.test {
        viewModel.onIntent(CreateProductionIntent.ToastDismissed)
        runCurrent()

        assertNull(expectMostRecentItem().errorToast)
      }
    }

  private fun TestScope.makeViewModel(repo: FakeProductionsRepository): CreateProductionViewModel =
    CreateProductionViewModel(
      createProductionUseCase = CreateProductionUseCase(repo),
      dispatcher = StandardTestDispatcher(testScheduler)
    )
}
