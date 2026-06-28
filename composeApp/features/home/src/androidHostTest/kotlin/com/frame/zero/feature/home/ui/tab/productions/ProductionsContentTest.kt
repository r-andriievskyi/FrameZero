package com.frame.zero.feature.home.ui.tab.productions

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.feature.home.tab.productions.ProductionUi
import com.frame.zero.feature.home.ui.tab.productions.ProductionsTabTestTags.EMPTY
import com.frame.zero.feature.home.ui.tab.productions.ProductionsTabTestTags.LIST
import com.frame.zero.shared.design_system.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Behaviour coverage for [ProductionsContent]: the paging-derived content state picks the empty
 * placeholder when there are no productions and the list when there are. (Skeleton/error require
 * forced `LoadStates`; the load-state→state mapping itself is unit-tested in the design system.)
 */
@RunWith(RobolectricTestRunner::class)
class ProductionsContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun showsTheEmptyStateWhenThereAreNoProductions() {
    setContent(PagingData.empty())

    composeRule.onNodeWithTag(EMPTY).assertIsDisplayed()
    composeRule.onNodeWithTag(LIST).assertDoesNotExist()
  }

  @Test
  fun showsTheListWhenProductionsArePresent() {
    setContent(PagingData.from(listOf(production("p1", "Echoes of Silence"))))

    composeRule.onNodeWithTag(LIST).assertIsDisplayed()
    composeRule.onNodeWithTag(EMPTY).assertDoesNotExist()
  }

  private fun setContent(pagingData: PagingData<ProductionUi>) {
    composeRule.setContent {
      AppTheme {
        val flow = remember { MutableStateFlow(pagingData) }
        val items = flow.collectAsLazyPagingItems()
        ProductionsContent(lazyPagingItems = items, onCreateProductionClick = {}, onProductionClick = {})
      }
    }
  }

  private fun production(
    id: String,
    title: String
  ) = ProductionUi(
    id = id,
    title = title,
    genre = Genre.DRAMA,
    phase = ProductionPhase.PRODUCTION,
    progressPercent = 50,
    daysLeft = 10,
    membersCount = 4
  )
}
