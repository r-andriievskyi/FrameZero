package com.frame.zero.feature.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.frame.zero.feature.home.tab.dashboard.DashboardTabComponent
import com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel
import com.frame.zero.feature.home.tab.projects.ProjectsTabComponent
import com.frame.zero.feature.home.tab.schedule.ScheduleTabComponent

class HomeComponent(
  componentContext: ComponentContext,
  val onNotificationsClick: () -> Unit = {},
  val onSettingsClick: () -> Unit = {},
  dashboardViewModelFactory: () -> DashboardTabViewModel,
) : ComponentContext by componentContext {

  val dashboardTab =
    DashboardTabComponent(
      componentContext = childContext(key = "tab-dashboard"),
      viewModelFactory = dashboardViewModelFactory,
    )
  val projectsTab = ProjectsTabComponent(childContext(key = "tab-projects"))
  val scheduleTab = ScheduleTabComponent(childContext(key = "tab-schedule"))
}
