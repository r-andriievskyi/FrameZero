package com.frame.zero.feature.auth.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.widgets.VerticalSpacer

@Composable
internal fun LabeledField(
  label: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val colorSystem = AppTheme.colorSystem
  Column(modifier = modifier) {
    Text(
      text = label,
      color = colorSystem.textPrimary,
      style = AppTheme.typographySystem.labelSmall
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    content()
  }
}
