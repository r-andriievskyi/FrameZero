package com.frame.zero.feature.home.ui

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frame.zero.feature.home.tab.HomeTab
import com.frame.zero.shared.design_system.AppTheme
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FloatingBottomNavA11yTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun tabsExposeTabRole() {
    composeRule.setContent {
      AppTheme {
        FloatingBottomNav(
          tabs = HomeTab.entries.toImmutableList(),
          selectedTab = HomeTab.DASHBOARD,
          onSelect = {}
        )
      }
    }

    composeRule.onNodeWithText("Dashboard")
      .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
  }

  @Test
  fun selectedTabReportsSelectedState() {
    composeRule.setContent {
      AppTheme {
        FloatingBottomNav(
          tabs = HomeTab.entries.toImmutableList(),
          selectedTab = HomeTab.DASHBOARD,
          onSelect = {}
        )
      }
    }

    composeRule.onNodeWithText("Dashboard").assertIsSelected()
    composeRule.onNodeWithText("Productions").assertIsNotSelected()
  }

  @Test
  fun tappingTabInvokesOnSelect() {
    val selected = mutableListOf<HomeTab>()
    composeRule.setContent {
      AppTheme {
        FloatingBottomNav(
          tabs = HomeTab.entries.toImmutableList(),
          selectedTab = HomeTab.DASHBOARD,
          onSelect = { selected += it }
        )
      }
    }

    composeRule.onNodeWithText("Schedule").performClick()

    assert(selected == listOf(HomeTab.SCHEDULE)) { "Expected [SCHEDULE], got $selected" }
  }
}
