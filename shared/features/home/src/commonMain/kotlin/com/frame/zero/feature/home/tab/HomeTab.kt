package com.frame.zero.feature.home.tab

/**
 * Tabs hosted by [com.frame.zero.feature.home.HomeComponent].
 *
 * Order in [entries] is the rendering order of the bottom nav AND the page order in the horizontal
 * pager — keep them aligned.
 */
enum class HomeTab(val title: String) {
  DASHBOARD("Dashboard"),
  PROJECTS("Projects"),
  SCHEDULE("Schedule"),
}
