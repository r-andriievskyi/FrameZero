package com.frame.zero.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.discovery.playground.shared.design_system.AppTheme
import com.frame.zero.feature.home.HomeComponent
import com.frame.zero.feature.home.tab.HomeTab
import com.frame.zero.feature.home.ui.tab.dashboard.DashboardTabContent
import com.frame.zero.feature.home.ui.tab.ProductionsTabContent
import com.frame.zero.feature.home.ui.tab.ScheduleTabContent
import kotlinx.coroutines.launch

@Composable
fun HomeContent(component: HomeComponent) {
  val tabs = HomeTab.entries
  val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
  val scope = rememberCoroutineScope()
  val activeTab = tabs[pagerState.currentPage]

  Box(modifier = Modifier.fillMaxSize().background(AppTheme.colorSystem.background)) {
    Column(modifier = Modifier.fillMaxSize()) {
      HomeToolbar(
        onNotificationsClick = component.onNotificationsClick,
        onSettingsClick = component.onSettingsClick,
        modifier = Modifier.statusBarsPadding()
      )
      HorizontalPager(
        state = pagerState,
        beyondViewportPageCount = 1,
        modifier = Modifier.weight(1f),
        key = { tabs[it] }
      ) { page ->
        when (tabs[page]) {
          HomeTab.DASHBOARD -> DashboardTabContent(component.dashboardTab)
          HomeTab.PRODUCTIONS -> ProductionsTabContent(component.projectsTab)
          HomeTab.SCHEDULE -> ScheduleTabContent(component.scheduleTab)
        }
      }
    }

    FloatingBottomNav(
      tabs = tabs,
      selectedTab = activeTab,
      onSelect = { tab -> scope.launch { pagerState.animateScrollToPage(tabs.indexOf(tab)) } },
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(AppTheme.spacingSystem.space16)
    )
  }
}
