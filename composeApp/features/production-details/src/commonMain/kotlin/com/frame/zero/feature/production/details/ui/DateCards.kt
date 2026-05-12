package com.frame.zero.feature.production.details.ui

import androidx.compose.foundation.background
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
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import kotlinx.datetime.LocalDate

@Composable
internal fun DateCards(
  startDate: LocalDate,
  wrapDate: LocalDate,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = AppTheme.spacingSystem.space16),
    horizontalArrangement = Arrangement.spacedBy(
      AppTheme.spacingSystem.space8
    )
  ) {
    DateCard(
      label = "START DATE",
      date = startDate.formatDisplay(),
      modifier = Modifier.weight(1f)
    )
    DateCard(
      label = "WRAP DATE",
      date = wrapDate.formatDisplay(),
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
      .background(AppTheme.colorSystem.cardBackground)
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

