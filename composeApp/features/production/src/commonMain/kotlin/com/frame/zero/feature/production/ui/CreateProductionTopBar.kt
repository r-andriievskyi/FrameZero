package com.frame.zero.feature.production.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.create_step_indicator
import framezero.composeapp.features.production.generated.resources.create_title
import framezero.composeapp.features.production.generated.resources.ic_chevron_left
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val BackButtonSize = 40.dp
private val StepIndicatorActiveWidth = 24.dp
private val StepIndicatorSize = 8.dp

// ── Top bar ──────────────────────────────────────────────────────────

@Composable
internal fun TopBar(
  step: Int,
  totalSteps: Int,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space16,
      ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(BackButtonSize)
        .clip(RoundedCornerShape(AppTheme.spacingSystem.space8))
        .background(AppTheme.colorSystem.cardBackground)
        .clickable(onClick = onBack),
      contentAlignment = Alignment.Center,
    ) {
      Image(
        painter = painterResource(Res.drawable.ic_chevron_left),
        contentDescription = null
      )
    }
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Column {
      Text(
        text = stringResource(Res.string.create_title),
        style = AppTheme.typographySystem.titleMedium,
        color = AppTheme.colorSystem.textPrimary,
      )
      Text(
        text = stringResource(Res.string.create_step_indicator, step, totalSteps),
        style = AppTheme.typographySystem.caption,
        color = AppTheme.colorSystem.textMuted,
      )
    }
  }
}

// ── Step indicator (dots) ────────────────────────────────────────────

@Composable
internal fun StepIndicator(
  currentStep: Int,
  totalSteps: Int,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    for (i in 1..totalSteps) {
      val isActive = i <= currentStep
      val shape = RoundedCornerShape(AppTheme.radiusSystem.radiusMax)
      Box(
        modifier = Modifier
          .width(if (i == currentStep) StepIndicatorActiveWidth else StepIndicatorSize)
          .height(StepIndicatorSize)
          .clip(shape)
          .background(
            if (isActive) AppTheme.colorSystem.accent
            else AppTheme.colorSystem.cardBackground,
          ),
      )
      if (i < totalSteps) HorizontalSpacer(AppTheme.spacingSystem.space4)
    }
  }
}

// ── Previews ─────────────────────────────────────────────────────────

@Preview
@Composable
private fun TopBarPreview() {
  AppTheme(darkTheme = true) {
    TopBar(step = 2, totalSteps = 3, onBack = {})
  }
}

@Preview
@Composable
private fun StepIndicatorPreview() {
  AppTheme(darkTheme = true) {
    StepIndicator(currentStep = 2, totalSteps = 3)
  }
}

