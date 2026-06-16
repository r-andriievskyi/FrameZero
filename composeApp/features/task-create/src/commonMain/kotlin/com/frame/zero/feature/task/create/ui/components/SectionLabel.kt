package com.frame.zero.feature.task.create.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview

@Composable
internal fun SectionLabel(
  text: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = text,
    style = AppTheme.typographySystem.caption.copy(fontWeight = FontWeight.Bold),
    color = AppTheme.colorSystem.textMuted,
    modifier = modifier
  )
}

@LightDarkPreview
@Composable
private fun SectionLabelPreview() {
  AppTheme {
    SectionLabel(text = "TITLE")
  }
}
