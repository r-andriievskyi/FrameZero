package com.frame.zero.feature.home.ui.tab

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.discovery.playground.shared.design_system.AppTheme
import com.frame.zero.feature.home.tab.dashboard.DashboardTabComponent

@Composable
fun DashboardTabContent(component: DashboardTabComponent) {
  LaunchedEffect(Unit) { component.onAppeared() }
  val state by component.state.collectAsState()

  Box(
    modifier =
      Modifier.fillMaxSize()
        .padding(
          horizontal = AppTheme.spacingSystem.space16,
          vertical = AppTheme.spacingSystem.space24,
        ),
    contentAlignment = Alignment.TopStart,
  ) {
    if (state.userName != null) {
      Text(
        text = "Hello, ${state.userName}",
        style = AppTheme.typographySystem.displayMedium,
        color = AppTheme.colorSystem.textPrimary,
      )
    }
  }
}
