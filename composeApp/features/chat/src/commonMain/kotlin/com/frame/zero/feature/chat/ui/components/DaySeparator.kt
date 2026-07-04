package com.frame.zero.feature.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.frame.zero.core.format.formatShort
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import framezero.composeapp.features.chat.generated.resources.Res
import framezero.composeapp.features.chat.generated.resources.chat_day_today
import framezero.composeapp.features.chat.generated.resources.chat_day_yesterday
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DaySeparator(
  day: LocalDate,
  today: LocalDate,
  modifier: Modifier = Modifier
) {
  val relative = when (day) {
    today -> stringResource(Res.string.chat_day_today)
    today.minus(DatePeriod(days = 1)) -> stringResource(Res.string.chat_day_yesterday)
    else -> null
  }
  // e.g. "TODAY · APR 26", or just "APR 26" for older days without a relative label.
  val label = (relative?.let { "$it · " }.orEmpty() + day.formatShort()).uppercase()
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = AppTheme.spacingSystem.space8),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = AppTheme.typographySystem.labelMedium,
      color = AppTheme.colorSystem.textMuted,
      modifier = Modifier
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radiusMax))
        .background(AppTheme.colorSystem.surfaceElevated)
        .padding(
          horizontal = AppTheme.spacingSystem.space12,
          vertical = AppTheme.spacingSystem.space4
        )
    )
  }
}

@LightDarkPreview
@Composable
private fun DaySeparatorPreview() {
  AppTheme {
    Box(modifier = Modifier.background(AppTheme.colorSystem.background)) {
      DaySeparator(day = LocalDate(2026, 7, 4), today = LocalDate(2026, 7, 4))
    }
  }
}
