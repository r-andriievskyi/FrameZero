package com.frame.zero.feature.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview

/**
 * A rounded pill with a centered label — the shared chip used by both the day separator and the
 * "New messages" divider. Callers supply the fill and content colors; the shape, padding, and
 * type are fixed so the two chips can't visually drift.
 */
@Composable
internal fun PillLabel(
  text: String,
  backgroundColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier
) {
  Text(
    text = text,
    style = AppTheme.typographySystem.labelMedium,
    color = contentColor,
    modifier = modifier
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radiusMax))
      .background(backgroundColor)
      .padding(
        horizontal = AppTheme.spacingSystem.space12,
        vertical = AppTheme.spacingSystem.space4
      )
  )
}

@LightDarkPreview
@Composable
private fun PillLabelPreview() {
  AppTheme {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16),
      verticalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
    ) {
      PillLabel(
        text = "TODAY · JUL 4",
        backgroundColor = AppTheme.colorSystem.surfaceElevated,
        contentColor = AppTheme.colorSystem.textMuted
      )
      PillLabel(
        text = "NEW MESSAGES",
        backgroundColor = AppTheme.colorSystem.accentDim,
        contentColor = AppTheme.colorSystem.accent
      )
    }
  }
}
