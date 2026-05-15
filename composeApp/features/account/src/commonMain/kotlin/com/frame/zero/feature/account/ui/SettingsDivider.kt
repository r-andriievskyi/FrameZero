package com.frame.zero.feature.account.ui

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import androidx.compose.foundation.layout.padding

private val DividerIndentStart = 72.dp

@Composable
internal fun SettingsDivider(modifier: Modifier = Modifier) {
  HorizontalDivider(
    modifier = modifier.padding(start = DividerIndentStart),
    color = AppTheme.colorSystem.cardBorder
  )
}

@Preview
@Composable
private fun SettingsDividerPreview() {
  AppTheme(darkTheme = true) {
    SettingsDivider()
  }
}
