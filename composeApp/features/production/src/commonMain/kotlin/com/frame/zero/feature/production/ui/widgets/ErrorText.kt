package com.frame.zero.feature.production.ui.widgets

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview

@Composable
internal fun ErrorText(
  error: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = error,
    style = AppTheme.typographySystem.bodySmall,
    color = AppTheme.colorSystem.errorText,
    modifier = modifier
  )
}

@LightDarkPreview
@Composable
private fun ErrorTextPreview() {
  AppTheme {
    ErrorText(error = "Title cannot be empty")
  }
}

