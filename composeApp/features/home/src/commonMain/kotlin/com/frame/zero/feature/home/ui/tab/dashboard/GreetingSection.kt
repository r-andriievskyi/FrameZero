package com.frame.zero.feature.home.ui.tab.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.dashboard.DashboardGreeting
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.greeting_good_morning
import framezero.composeapp.features.home.generated.resources.greeting_stats
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GreetingSection(greeting: DashboardGreeting) {
  Text(
    text = stringResource(Res.string.greeting_good_morning, greeting.displayName),
    style = AppTheme.typographySystem.displayMedium,
    color = AppTheme.colorSystem.textPrimary
  )
  VerticalSpacer(AppTheme.spacingSystem.space4)
  Text(
    text = stringResource(
      Res.string.greeting_stats,
      greeting.activeProductionsCount,
      greeting.openTasksCount
    ),
    style = AppTheme.typographySystem.bodyMedium,
    color = AppTheme.colorSystem.textSecondary
  )
}

@Preview
@Composable
private fun GreetingSectionPreview() {
  AppTheme(darkTheme = true) {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      GreetingSection(
        greeting = DashboardGreeting(
          displayName = "Maya",
          activeProductionsCount = 3,
          openTasksCount = 12
        )
      )
    }
  }
}
