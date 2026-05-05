package com.frame.zero.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import com.discovery.playground.shared.design_system.AppTheme
import com.frame.zero.feature.home.tab.HomeTab

/** Stateless. The container owns selection state; this just renders + reports clicks. */
@Composable
fun FloatingBottomNav(
  tabs: List<HomeTab>,
  selectedTab: HomeTab,
  onSelect: (HomeTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radiusMax)
  Row(
    modifier =
      modifier
        .shadow(elevation = AppTheme.spacingSystem.space8, shape = shape)
        .clip(shape)
        .background(AppTheme.colorSystem.surfaceElevated)
        .padding(
          horizontal = AppTheme.spacingSystem.space8,
          vertical = AppTheme.spacingSystem.space4,
        ),
    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space4),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    tabs.forEach { tab ->
      NavItem(tab = tab, selected = tab == selectedTab, onClick = { onSelect(tab) })
    }
  }
}

@Composable
private fun NavItem(
  tab: HomeTab,
  selected: Boolean,
  onClick: () -> Unit
) {
  val colors = AppTheme.colorSystem
  val background = if (selected) colors.accentSurface else Color.Transparent
  val textColor = if (selected) colors.accentText else colors.textSecondary
  Box(
    modifier =
      Modifier.clip(RoundedCornerShape(AppTheme.radiusSystem.radiusMax))
        .clickable(onClick = onClick)
        .background(background)
        .padding(
          horizontal = AppTheme.spacingSystem.space16,
          vertical = AppTheme.spacingSystem.space8,
        ),
    contentAlignment = Alignment.Center,
  ) {
    Text(text = tab.title, style = AppTheme.typographySystem.labelMedium, color = textColor)
  }
}
