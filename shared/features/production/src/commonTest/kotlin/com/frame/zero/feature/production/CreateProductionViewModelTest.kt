package com.frame.zero.feature.production

import com.frame.zero.core.network.connectivity.OfflineException
import com.frame.zero.feature.production.domain.CreateProductionUseCase
import com.frame.zero.testing.FakeProductionsRepository
import com.frame.zero.ui.asUiText
import framezero.shared.features.production.generated.resources.Res
import framezero.shared.features.production.generated.resources.error_invalid_dates
import framezero.shared.features.production.generated.resources.error_missing_dates
import framezero.shared.features.production.generated.resources.error_network
import framezero.shared.features.production.generated.resources.error_title_required
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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

      viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))
      viewModel.onIntent(CreateProductionIntent.StartDateChanged(start))
      viewModel.onIntent(CreateProductionIntent.WrapDateChanged(wrap))

      assertTrue(viewModel.state.value.canAdvanceStep1)
    }

  @Test
  fun `step 1 cannot advance when wrap date is not after start date`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())

      viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))
      viewModel.onIntent(CreateProductionIntent.StartDateChanged(start))
      viewModel.onIntent(CreateProductionIntent.WrapDateChanged(start))

      assertEquals(false, viewModel.state.value.canAdvanceStep1)
    }

  @Test
  fun `next step on blank title surfaces title-required error`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())

      viewModel.onIntent(CreateProductionIntent.NextStep)

      assertEquals(Res.string.error_title_required.asUiText(), viewModel.state.value.error)
      assertEquals(1, viewModel.state.value.currentStep)
    }

  @Test
  fun `next step with title but invalid dates surfaces invalid-dates error`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())

      viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))
      viewModel.onIntent(CreateProductionIntent.NextStep)

      assertEquals(Res.string.error_invalid_dates.asUiText(), viewModel.state.value.error)
    }

  @Test
  fun `next then previous step navigates between steps`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))
      viewModel.onIntent(CreateProductionIntent.StartDateChanged(start))
      viewModel.onIntent(CreateProductionIntent.WrapDateChanged(wrap))

      viewModel.onIntent(CreateProductionIntent.NextStep)
      assertEquals(2, viewModel.state.value.currentStep)

      viewModel.onIntent(CreateProductionIntent.PreviousStep)
      assertEquals(1, viewModel.state.value.currentStep)
    }

  @Test
  fun `budget changed formats display with thousands separators`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())

      viewModel.onIntent(CreateProductionIntent.BudgetChanged(123_456))
      assertEquals("$1,234", viewModel.state.value.budgetDisplay)

      viewModel.onIntent(CreateProductionIntent.BudgetChanged(-123_456))
      assertEquals("-$1,234", viewModel.state.value.budgetDisplay)
    }

  @Test
  fun `add crew member trims name and resets inputs`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      viewModel.onIntent(CreateProductionIntent.CrewNameChanged("  Ada  "))
      viewModel.onIntent(CreateProductionIntent.CrewRoleChanged("Producer"))

      viewModel.onIntent(CreateProductionIntent.AddCrewMember)

      val crew = viewModel.state.value.crewMembers
      assertEquals(1, crew.size)
      assertEquals("Ada", crew.single().name)
      assertEquals("Producer", crew.single().role)
      assertEquals("", viewModel.state.value.crewNameInput)
      assertEquals(DEFAULT_CREW_ROLE, viewModel.state.value.crewRoleInput)
    }

  @Test
  fun `add crew member ignores blank name`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      viewModel.onIntent(CreateProductionIntent.CrewNameChanged("   "))

      viewModel.onIntent(CreateProductionIntent.AddCrewMember)

      assertTrue(viewModel.state.value.crewMembers.isEmpty())
    }

  @Test
  fun `remove crew member drops the entry at the index`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      viewModel.onIntent(CreateProductionIntent.CrewNameChanged("Ada"))
      viewModel.onIntent(CreateProductionIntent.AddCrewMember)
      viewModel.onIntent(CreateProductionIntent.CrewNameChanged("Bo"))
      viewModel.onIntent(CreateProductionIntent.AddCrewMember)

      viewModel.onIntent(CreateProductionIntent.RemoveCrewMember(0))

      assertEquals(listOf("Bo"), viewModel.state.value.crewMembers.map { it.name })
    }

  @Test
  fun `submit without dates surfaces missing-dates error`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))

      viewModel.onIntent(CreateProductionIntent.Submit)
      advanceUntilIdle()

      assertEquals(Res.string.error_missing_dates.asUiText(), viewModel.state.value.error)
      assertEquals(false, viewModel.state.value.isLoading)
    }

  @Test
  fun `submit success emits navigation event`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository())
      viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))
      viewModel.onIntent(CreateProductionIntent.StartDateChanged(start))
      viewModel.onIntent(CreateProductionIntent.WrapDateChanged(wrap))
      val events = mutableListOf<Unit>()
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        viewModel.navigationEvents.collect { events += it }
      }

      viewModel.onIntent(CreateProductionIntent.Submit)
      advanceUntilIdle()

      assertEquals(1, events.size)
      assertNull(viewModel.state.value.error)
      assertEquals(false, viewModel.state.value.isLoading)
    }

  @Test
  fun `submit network failure surfaces a toast not an inline error`() =
    runTest {
      val viewModel = makeViewModel(FakeProductionsRepository(createThrows = OfflineException()))
      viewModel.onIntent(CreateProductionIntent.TitleChanged("Pilot"))
      viewModel.onIntent(CreateProductionIntent.StartDateChanged(start))
      viewModel.onIntent(CreateProductionIntent.WrapDateChanged(wrap))

      viewModel.onIntent(CreateProductionIntent.Submit)
      advanceUntilIdle()

      assertEquals(Res.string.error_network.asUiText(), viewModel.state.value.errorToast)
      assertNull(viewModel.state.value.error)
      assertEquals(false, viewModel.state.value.isLoading)
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

      viewModel.onIntent(CreateProductionIntent.ToastDismissed)

      assertNull(viewModel.state.value.errorToast)
    }

  private fun TestScope.makeViewModel(repo: FakeProductionsRepository): CreateProductionViewModel =
    CreateProductionViewModel(
      createProductionUseCase = CreateProductionUseCase(repo),
      dispatcher = StandardTestDispatcher(testScheduler)
    )
}
