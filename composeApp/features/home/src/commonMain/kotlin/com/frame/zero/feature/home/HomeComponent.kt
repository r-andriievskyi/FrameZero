package com.frame.zero.feature.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.frame.zero.feature.home.tab.dashboard.DashboardTabComponent
import com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel
import com.frame.zero.feature.home.tab.productions.ProductionsTabComponent
import com.frame.zero.feature.home.tab.productions.ProductionsTabViewModel
import com.frame.zero.feature.home.tab.schedule.ScheduleTabComponent
import com.frame.zero.feature.home.tab.schedule.ScheduleTabViewModel

class HomeComponent(
  componentContext: ComponentContext,
  val onNotificationsClick: () -> Unit = {},
  val onAccountClick: () -> Unit = {},
  val onCreateProductionClick: () -> Unit = {},
  val onProductionClick: (productionId: String) -> Unit = {},
  val onTaskClick: (taskId: String) -> Unit = {},
  val onTasksClick: () -> Unit = {},
  dashboardViewModelFactory: () -> DashboardTabViewModel,
  productionsViewModelFactory: () -> ProductionsTabViewModel,
  scheduleViewModelFactory: () -> ScheduleTabViewModel
) : ComponentContext by componentContext {
  val dashboardTab = DashboardTabComponent(
    componentContext = childContext(key = "tab-dashboard"),
    onTaskClick = onTaskClick,
    onTasksClick = onTasksClick,
    viewModelFactory = dashboardViewModelFactory
  )
  val productionsTab = ProductionsTabComponent(
    componentContext = childContext(key = "tab-productions"),
    onCreateProductionClick = onCreateProductionClick,
    onProductionClick = onProductionClick,
    viewModelFactory = productionsViewModelFactory
  )
  val scheduleTab = ScheduleTabComponent(
    componentContext = childContext(key = "tab-schedule"),
    onTaskClick = onTaskClick,
    viewModelFactory = scheduleViewModelFactory
  )
}
