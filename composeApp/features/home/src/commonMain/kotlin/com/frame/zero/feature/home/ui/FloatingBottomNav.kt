package com.frame.zero.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frame.zero.feature.home.tab.HomeTab
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.tab_dashboard
import framezero.composeapp.features.home.generated.resources.tab_productions
import framezero.composeapp.features.home.generated.resources.tab_schedule
import org.jetbrains.compose.resources.stringResource

private val Height = 65.dp
private val BorderWidth = 1.dp

@Composable
private fun HomeTab.label(): String =
  when (this) {
    HomeTab.DASHBOARD -> stringResource(Res.string.tab_dashboard)
    HomeTab.PRODUCTIONS -> stringResource(Res.string.tab_productions)
    HomeTab.SCHEDULE -> stringResource(Res.string.tab_schedule)
  }

@Composable
fun FloatingBottomNav(
  tabs: List<HomeTab>,
  selectedTab: HomeTab,
  onSelect: (HomeTab) -> Unit,
  modifier: Modifier = Modifier
) {
  val radius = AppTheme.radiusSystem.radiusMax
  val shape = remember(radius) { RoundedCornerShape(radius) }
  Row(
    modifier = modifier
      .height(Height)
      .border(width = BorderWidth, color = AppTheme.colorSystem.border, shape = shape)
      .clip(shape)
      .background(AppTheme.colorSystem.surfaceElevated)
      .padding(AppTheme.spacingSystem.space4),
    verticalAlignment = Alignment.CenterVertically
  ) {
    tabs.forEach { tab ->
      NavItem(
        modifier = Modifier.fillMaxHeight().weight(1f),
        tab = tab,
        selected = tab == selectedTab,
        onClick = { onSelect(tab) }
      )
    }
  }
}

@Composable
private fun NavItem(
  modifier: Modifier = Modifier,
  tab: HomeTab,
  selected: Boolean,
  onClick: () -> Unit
) {
  val colorSystem = AppTheme.colorSystem
  val itemRadius = AppTheme.radiusSystem.radiusMax
  val itemShape = remember(itemRadius) { RoundedCornerShape(itemRadius) }
  Box(
    modifier = modifier
      .clip(itemShape)
      .clickableWithRipple(
        color = AppTheme.colorSystem.accentDim,
        onClick = onClick
      )
      .background(color = if (selected) colorSystem.accent else Color.Transparent)
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space8
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = tab.label(),
      style = AppTheme.typographySystem.labelMedium,
      color = if (selected) colorSystem.textOnAccent else colorSystem.textSecondary,
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun FloatingBottomNavPreview() {
  AppTheme {
    var selected by remember { mutableStateOf(HomeTab.DASHBOARD) }
    FloatingBottomNav(
      tabs = HomeTab.entries,
      selectedTab = selected,
      onSelect = { selected = it }
    )
  }
}
