package com.frame.zero.shared.design_system.widgets

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview

@Composable
fun ErrorText(
  text: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = text,
    style = AppTheme.typographySystem.bodySmall,
    color = AppTheme.colorSystem.errorText,
    modifier = modifier
  )
}

@LightDarkPreview
@Composable
private fun ErrorTextPreview() {
  AppTheme {
    ErrorText(text = "Title cannot be empty")
  }
}
