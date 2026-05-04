package com.frame.zero.feature.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.frame.zero.feature.home.tab.dashboard.DashboardTabComponent
import com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel
import com.frame.zero.feature.home.tab.projects.ProjectsTabComponent
import com.frame.zero.feature.home.tab.projects.ProjectsTabViewModel
import com.frame.zero.feature.home.tab.schedule.ScheduleTabComponent
import com.frame.zero.feature.home.tab.schedule.ScheduleTabViewModel

class HomeComponent(
  componentContext: ComponentContext,
  val onNotificationsClick: () -> Unit = {},
  val onSettingsClick: () -> Unit = {},
  dashboardViewModelFactory: () -> DashboardTabViewModel,
  projectsViewModelFactory: () -> ProjectsTabViewModel,
  scheduleViewModelFactory: () -> ScheduleTabViewModel,
) : ComponentContext by componentContext {

  val dashboardTab =
    DashboardTabComponent(
      componentContext = childContext(key = "tab-dashboard"),
      viewModelFactory = dashboardViewModelFactory,
    )
  val projectsTab =
    ProjectsTabComponent(
      componentContext = childContext(key = "tab-projects"),
      viewModelFactory = projectsViewModelFactory,
    )
  val scheduleTab =
    ScheduleTabComponent(
      componentContext = childContext(key = "tab-schedule"),
      viewModelFactory = scheduleViewModelFactory,
    )
}
