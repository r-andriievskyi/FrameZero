package com.frame.zero.feature.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.frame.zero.feature.home.tab.dashboard.DashboardTabComponent
import com.frame.zero.feature.home.tab.projects.ProjectsTabComponent
import com.frame.zero.feature.home.tab.schedule.ScheduleTabComponent

/**
 * Container hosting the bottom-nav + horizontal pager. Owns nothing but tab Components — no
 * business state, no data loading.
 *
 * Why all tabs are constructed eagerly:
 * - HorizontalPager needs stable items per page index. The Components live for the full lifetime of
 *   [HomeComponent], so each page always has a destination.
 * - State preservation falls out for free: each tab gets its own [childContext] (= isolated
 *   `instanceKeeper`), so its VM survives recomposition and tab swipes.
 *
 * Why loading does NOT run in VM `init`:
 * - All Components are created at app start — if init fetched, every tab would load on launch
 *   regardless of pager visibility.
 * - Instead, each VM exposes a one-shot `onAppeared()` and the UI fires it from a `LaunchedEffect`
 *   when that page first composes. Combined with `beyondViewportPageCount`, this gives the desired
 *   "preload immediate neighbors, lazy-load the rest" behavior.
 */
class HomeComponent(
  componentContext: ComponentContext,
  val onNotificationsClick: () -> Unit = {},
  val onSettingsClick: () -> Unit = {},
) : ComponentContext by componentContext {

  // childContext keys must be unique within this parent — they namespace the instanceKeeper.
  val dashboardTab = DashboardTabComponent(childContext(key = "tab-dashboard"))
  val projectsTab = ProjectsTabComponent(childContext(key = "tab-projects"))
  val scheduleTab = ScheduleTabComponent(childContext(key = "tab-schedule"))
}
