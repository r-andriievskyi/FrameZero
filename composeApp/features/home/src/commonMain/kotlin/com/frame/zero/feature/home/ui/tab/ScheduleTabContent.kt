package com.frame.zero.feature.home.ui.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.discovery.playground.shared.design_system.AppTheme
import com.frame.zero.feature.home.tab.schedule.ScheduleTabComponent

/** Empty stub. See DashboardTabContent for the load-on-first-appear contract. */
@Composable
fun ScheduleTabContent(component: ScheduleTabComponent) {
  LaunchedEffect(Unit) { component.onAppeared() }
  Box(modifier = Modifier.fillMaxSize().background(AppTheme.colorSystem.background))
}
