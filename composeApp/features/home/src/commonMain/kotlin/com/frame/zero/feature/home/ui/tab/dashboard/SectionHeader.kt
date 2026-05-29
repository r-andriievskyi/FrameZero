package com.frame.zero.feature.home.ui.tab.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme

@Composable
internal fun SectionHeader(
  title: String,
  actionLabel: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = AppTheme.typographySystem.titleMedium,
      color = AppTheme.colorSystem.textPrimary
    )
    Text(
      text = actionLabel,
      style = AppTheme.typographySystem.labelMedium,
      color = AppTheme.colorSystem.accentText
    )
  }
}
