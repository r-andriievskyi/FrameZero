package com.frame.zero.feature.production.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.feature.production.ui.SelectedBorderWidth
import com.frame.zero.feature.production.ui.dotColor
import com.frame.zero.feature.production.ui.label
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer

private val PhaseDotSize = 12.dp

private val visiblePhases = listOf(
  ProductionPhase.IDEA,
  ProductionPhase.DEVELOPMENT,
  ProductionPhase.FINANCING,
  ProductionPhase.PRE_PRODUCTION,
  ProductionPhase.PRODUCTION,
  ProductionPhase.POST_PRODUCTION,
  ProductionPhase.MARKETING,
  ProductionPhase.DISTRIBUTION,
  ProductionPhase.RELEASE
)

@Composable
internal fun PhaseSelector(
  selected: ProductionPhase,
  onSelect: (ProductionPhase) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
  ) {
    visiblePhases.forEach { phase ->
      val isSelected = phase == selected
      val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
      val borderColor = if (isSelected) phase.dotColor() else AppTheme.colorSystem.cardBorder
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(shape)
          .background(AppTheme.colorSystem.cardBackground)
          .border(
            width = if (isSelected) SelectedBorderWidth else AppTheme.borderSystem.hairline,
            color = borderColor,
            shape = shape
          )
          .clickable { onSelect(phase) }
          .padding(
            horizontal = AppTheme.spacingSystem.space16,
            vertical = AppTheme.spacingSystem.space16
          ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(PhaseDotSize)
              .clip(CircleShape)
              .background(phase.dotColor())
          )
          HorizontalSpacer(AppTheme.spacingSystem.space8)
          Text(
            text = phase.label(),
            style = AppTheme.typographySystem.bodyMedium,
            color = if (isSelected) {
              AppTheme.colorSystem.textPrimary
            } else {
              AppTheme.colorSystem.textMuted
            }
          )
        }
      }
    }
  }
}

@LightDarkPreview
@Composable
private fun PhaseSelectorPreview() {
  AppTheme {
    PhaseSelector(
      selected = ProductionPhase.PRE_PRODUCTION,
      onSelect = {}
    )
  }
}

