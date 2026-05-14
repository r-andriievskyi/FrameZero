package com.frame.zero.feature.production.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.HorizontalSpacer
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.ProductionPipelinePhase

private val PhaseBarSegmentHeight = 6.dp
private val PhaseDotSize = 16.dp

@Composable
internal fun PipelineCard(
  pipeline: List<ProductionPipelinePhase>,
  currentPhase: ProductionPhase,
  modifier: Modifier = Modifier
) {
  val currentIndex = pipeline.indexOfFirst { it.isCurrent }
  val totalPhases = pipeline.size

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = AppTheme.spacingSystem.space16)
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "PIPELINE",
        style = AppTheme.typographySystem.caption,
        color = AppTheme.colorSystem.textMuted
      )
      Text(
        text = "Phase ${currentIndex + 1} of $totalPhases",
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
    VerticalSpacer(AppTheme.spacingSystem.space16)

    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(PhaseDotSize)
          .clip(CircleShape)
          .background(phaseAccentColor(currentPhase))
      )
      HorizontalSpacer(AppTheme.spacingSystem.space8)
      Text(
        text = currentPhase.displayLabel(),
        style = AppTheme.typographySystem.titleLarge,
        color = AppTheme.colorSystem.textPrimary
      )
    }
    VerticalSpacer(AppTheme.spacingSystem.space16)

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(
        AppTheme.spacingSystem.space4
      )
    ) {
      pipeline.forEach { phase ->
        val segmentColor = when {
          phase.isCompleted || phase.isCurrent ->
            phaseAccentColor(phase.phase)

          else -> AppTheme.colorSystem.border
        }
        Box(
          modifier = Modifier
            .weight(1f)
            .height(PhaseBarSegmentHeight)
            .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
            .background(segmentColor)
        )
      }
    }
  }
}

@Preview
@Composable
private fun PipelineCardPreview() {
  val pipeline = listOf(
    ProductionPipelinePhase(
      phase = ProductionPhase.DEVELOPMENT,
      label = "Development",
      isCompleted = true,
      isCurrent = false
    ),
    ProductionPipelinePhase(
      phase = ProductionPhase.PRE_PRODUCTION,
      label = "Pre-Production",
      isCompleted = false,
      isCurrent = true
    ),
    ProductionPipelinePhase(
      phase = ProductionPhase.PRODUCTION,
      label = "Production",
      isCompleted = false,
      isCurrent = false
    ),
    ProductionPipelinePhase(
      phase = ProductionPhase.POST_PRODUCTION,
      label = "Post-Production",
      isCompleted = false,
      isCurrent = false
    )
  )
  AppTheme {
    PipelineCard(
      pipeline = pipeline,
      currentPhase = ProductionPhase.PRE_PRODUCTION
    )
  }
}

