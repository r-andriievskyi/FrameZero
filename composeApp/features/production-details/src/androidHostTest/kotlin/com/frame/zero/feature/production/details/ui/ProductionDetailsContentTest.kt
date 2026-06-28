package com.frame.zero.feature.production.details.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frame.zero.feature.production.details.ProductionDetailUi
import com.frame.zero.feature.production.details.ProductionDetailsIntent
import com.frame.zero.feature.production.details.ProductionDetailsState
import com.frame.zero.feature.production.details.ui.ProductionDetailsTestTags.CONTENT
import com.frame.zero.feature.production.details.ui.ProductionDetailsTestTags.ERROR
import com.frame.zero.feature.production.details.ui.ProductionDetailsTestTags.LOADING
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.ui.UiText
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Behaviour (not screenshot) coverage for [ProductionDetailsContent]: the three mutually-exclusive
 * load states the `when` selects, and that the error state's retry control wires back to a
 * [ProductionDetailsIntent.Refresh]. Mirrors `DashboardTabContentTest`.
 */
@RunWith(RobolectricTestRunner::class)
class ProductionDetailsContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun showsOnlyTheProgressIndicatorWhileLoadingWithNoDetail() {
    setContent(ProductionDetailsState(isLoading = true, detail = null))

    composeRule.onNodeWithTag(LOADING).assertIsDisplayed()
    composeRule.onNodeWithTag(ERROR).assertDoesNotExist()
    composeRule.onNodeWithTag(CONTENT).assertDoesNotExist()
  }

  @Test
  fun showsOnlyTheErrorWithItsMessageWhenLoadFailsWithNoDetail() {
    setContent(ProductionDetailsState(error = UiText.Dynamic("Could not load production"), detail = null))

    composeRule.onNodeWithTag(ERROR).assertIsDisplayed()
    composeRule.onNodeWithText("Could not load production").assertIsDisplayed()
    composeRule.onNodeWithTag(LOADING).assertDoesNotExist()
    composeRule.onNodeWithTag(CONTENT).assertDoesNotExist()
  }

  @Test
  fun showsOnlyTheContentWithTheTitleWhenDetailLoaded() {
    setContent(ProductionDetailsState(detail = detailUi(title = "Echoes of Silence")))

    composeRule.onNodeWithTag(CONTENT).assertIsDisplayed()
    composeRule.onNodeWithText("Echoes of Silence").assertIsDisplayed()
    composeRule.onNodeWithTag(LOADING).assertDoesNotExist()
    composeRule.onNodeWithTag(ERROR).assertDoesNotExist()
  }

  @Test
  fun tappingRetryInTheErrorStateEmitsRefresh() {
    val intents = mutableListOf<ProductionDetailsIntent>()
    setContent(
      state = ProductionDetailsState(error = UiText.Dynamic("offline"), detail = null),
      onIntent = { intents += it }
    )

    composeRule.onNodeWithText("Retry").performClick()

    assert(intents == listOf(ProductionDetailsIntent.Refresh)) { "Expected [Refresh], got $intents" }
  }

  private fun setContent(
    state: ProductionDetailsState,
    onIntent: (ProductionDetailsIntent) -> Unit = {}
  ) {
    composeRule.setContent {
      AppTheme {
        ProductionDetailsContent(state = state, onBack = {}, onIntent = onIntent, onAddTask = {})
      }
    }
  }

  private fun detailUi(title: String): ProductionDetailUi =
    ProductionDetailUi(
      title = title,
      logline = null,
      phase = ProductionPhase.PRODUCTION,
      progressPercent = 40,
      daysLeft = 12,
      membersCount = 3,
      budgetLabel = "$1.5M",
      startDateLabel = "Apr 1",
      wrapDateLabel = "May 1",
      pipeline = persistentListOf(),
      viewerCrew = null
    )
}
