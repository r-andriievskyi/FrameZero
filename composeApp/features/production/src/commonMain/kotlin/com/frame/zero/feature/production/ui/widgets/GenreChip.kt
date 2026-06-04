package com.frame.zero.feature.production.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple

@Composable
internal fun GenreChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radiusMax)
  Box(
    modifier = modifier
      .clip(shape)
      .background(
        if (isSelected) {
          AppTheme.colorSystem.accentSurface
        } else {
          AppTheme.colorSystem.cardBackground
        }
      )
      .border(
        AppTheme.borderSystem.hairline,
        if (isSelected) AppTheme.colorSystem.accent else AppTheme.colorSystem.cardBorder,
        shape
      )
      .clickableWithRipple(color = AppTheme.colorSystem.accentDim, onClick = onClick)
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space8
      )
  ) {
    Text(
      text = label,
      style = AppTheme.typographySystem.labelMedium,
      color = if (isSelected) {
        AppTheme.colorSystem.accentText
      } else {
        AppTheme.colorSystem.textSecondary
      }
    )
  }
}

@LightDarkPreview
@Composable
private fun GenreChipSelectedPreview() {
  AppTheme {
    GenreChip(label = "Drama", isSelected = true, onClick = {})
  }
}

@LightDarkPreview
@Composable
private fun GenreChipUnselectedPreview() {
  AppTheme {
    GenreChip(label = "Comedy", isSelected = false, onClick = {})
  }
}

