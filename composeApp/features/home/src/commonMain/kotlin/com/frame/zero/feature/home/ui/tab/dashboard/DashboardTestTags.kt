package com.frame.zero.feature.home.ui.tab.dashboard

internal object DashboardTestTags {
  const val GREETING = "dashboard:greeting"
  const val STAT_ACTIVE_PRODUCTIONS = "dashboard:stat:active-productions"
  const val STAT_OPEN_TASKS = "dashboard:stat:open-tasks"
  const val MY_TASKS_SECTION = "dashboard:my-tasks:section"

  fun taskRow(id: String) = "dashboard:my-tasks:row:$id"
}
