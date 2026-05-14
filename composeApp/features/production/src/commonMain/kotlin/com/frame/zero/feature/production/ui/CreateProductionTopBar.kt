package com.frame.zero.feature.production.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.HorizontalSpacer

private val StepIndicatorActiveWidth = 24.dp
private val StepIndicatorSize = 8.dp

@Composable
internal fun StepIndicator(
  currentStep: Int,
  totalSteps: Int,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
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
            if (isActive) {
              AppTheme.colorSystem.accent
            } else {
              AppTheme.colorSystem.cardBackground
            }
          )
      )
      if (i < totalSteps) HorizontalSpacer(AppTheme.spacingSystem.space4)
    }
  }
}

@Preview
@Composable
private fun StepIndicatorPreview() {
  AppTheme(darkTheme = true) {
    StepIndicator(currentStep = 2, totalSteps = 3)
  }
}
