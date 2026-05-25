package com.frame.zero.shared.design_system

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ColorSystem(
  val background: Color,
  val surfaceElevated: Color,
  val navBackground: Color,
  val inputBackground: Color,
  val cardBackground: Color,
  val border: Color,
  val cardBorder: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val textMuted: Color,
  val textOnAccent: Color,
  val accent: Color,
  val accentDim: Color,
  val accentSurface: Color,
  val accentText: Color,
  val successSurface: Color,
  val successText: Color,
  val warningSurface: Color,
  val warningText: Color,
  val errorSurface: Color,
  val errorText: Color,
  val priorityHighSurface: Color,
  val priorityHighText: Color,
  val priorityMedSurface: Color,
  val priorityMedText: Color,
  val priorityLowSurface: Color,
  val priorityLowText: Color,
  val developmentSurface: Color,
  val developmentText: Color,
  val preProductionSurface: Color,
  val preProductionText: Color,
  val productionSurface: Color,
  val productionText: Color,
  val postProductionSurface: Color,
  val postProductionText: Color,
  val distributionSurface: Color,
  val distributionText: Color
)
