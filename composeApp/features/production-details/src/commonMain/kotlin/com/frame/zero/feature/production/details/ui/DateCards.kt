package com.frame.zero.feature.production.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.production_details.generated.resources.Res
import framezero.composeapp.features.production_details.generated.resources.start_date
import framezero.composeapp.features.production_details.generated.resources.wrap_date
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DateCards(
  startDate: String,
  wrapDate: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = AppTheme.spacingSystem.space16),
    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
  ) {
    DateCard(
      label = stringResource(Res.string.start_date),
      date = startDate,
      modifier = Modifier.weight(1f)
    )
    DateCard(
      label = stringResource(Res.string.wrap_date),
      date = wrapDate,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun DateCard(
  label: String,
  date: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
      .background(AppTheme.colorSystem.cardBackground, RoundedCornerShape(AppTheme.radiusSystem.radius16))
      .border(
        AppTheme.borderSystem.hairline,
        AppTheme.colorSystem.border,
        RoundedCornerShape(AppTheme.radiusSystem.radius16)
      )
      .padding(AppTheme.spacingSystem.space16)
  ) {
    Text(
      text = label,
      style = AppTheme.typographySystem.caption,
      color = AppTheme.colorSystem.textMuted
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    Text(
      text = date,
      style = AppTheme.typographySystem.titleMedium,
      color = AppTheme.colorSystem.textPrimary
    )
  }
}

@LightDarkPreview
@Composable
private fun DateCardsPreview() {
  AppTheme {
    DateCards(
      startDate = "Mar 15, 2025",
      wrapDate = "Jun 30, 2025"
    )
  }
}
