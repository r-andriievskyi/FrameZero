package com.frame.zero.feature.production.ui.widgets

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview

@Composable
internal fun FieldLabel(
  text: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = text,
    style = AppTheme.typographySystem.labelSmall,
    color = AppTheme.colorSystem.textSecondary,
    modifier = modifier
  )
}

@LightDarkPreview
@Composable
private fun FieldLabelPreview() {
  AppTheme {
    FieldLabel(text = "Production Title")
  }
}

