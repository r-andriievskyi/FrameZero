package com.frame.zero.shared.design_system

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frame.zero.design.tokens.ColorTokens
import com.frame.zero.design.tokens.CubicBezier
import com.frame.zero.design.tokens.DesignTokens
import com.frame.zero.design.tokens.FontFamilyToken
import com.frame.zero.design.tokens.TextStyleToken
import com.frame.zero.design.tokens.TypographyTokens

/** Compose view of [DesignTokens] (`shared/design-tokens`), the values SwiftUI reads too. */
@Immutable
data class ThemeOptions(
  val colorSystem: ColorSystem,
  val typographySystem: TypographySystem,
  val spacingSystem: SpacingSystem,
  val radiusSystem: RadiusSystem,
  val borderSystem: BorderSystem,
  val motionSystem: MotionSystem
) {
  companion object {
    fun light() = themeOptions(DesignTokens.light)

    fun dark() = themeOptions(DesignTokens.dark)

    private fun themeOptions(colors: ColorTokens) =
      ThemeOptions(
        colorSystem = colors.toColorSystem(),
        typographySystem = DesignTokens.typography.toTypographySystem(),
        spacingSystem = with(DesignTokens.spacing) {
          SpacingSystem(
            space2 = space2.dp,
            space4 = space4.dp,
            space8 = space8.dp,
            space12 = space12.dp,
            space16 = space16.dp,
            space24 = space24.dp,
            space32 = space32.dp
          )
        },
        radiusSystem = with(DesignTokens.radius) {
          RadiusSystem(
            radius4 = radius4.dp,
            radius8 = radius8.dp,
            radius14 = radius14.dp,
            radius16 = radius16.dp,
            radiusMax = radiusMax.dp
          )
        },
        borderSystem = BorderSystem(hairline = DesignTokens.border.hairline.dp),
        motionSystem = with(DesignTokens.motion) {
          MotionSystem(
            durationFast = durationFast,
            durationMedium = durationMedium,
            durationSlow = durationSlow,
            durationLoop = durationLoop,
            easingStandard = easingStandard.toEasing(),
            easingEmphasized = easingEmphasized.toEasing(),
            easingDecelerate = easingDecelerate.toEasing(),
            easingAccelerate = easingAccelerate.toEasing(),
            easingLinear = easingLinear.toEasing()
          )
        }
      )
  }
}

private fun ColorTokens.toColorSystem() =
  ColorSystem(
    background = Color(background),
    surfaceElevated = Color(surfaceElevated),
    navBackground = Color(navBackground),
    inputBackground = Color(inputBackground),
    cardBackground = Color(cardBackground),
    border = Color(border),
    cardBorder = Color(cardBorder),
    textPrimary = Color(textPrimary),
    textSecondary = Color(textSecondary),
    textMuted = Color(textMuted),
    textOnAccent = Color(textOnAccent),
    accent = Color(accent),
    accentDim = Color(accentDim),
    accentSurface = Color(accentSurface),
    accentText = Color(accentText),
    successSurface = Color(successSurface),
    successText = Color(successText),
    warningSurface = Color(warningSurface),
    warningText = Color(warningText),
    errorSurface = Color(errorSurface),
    errorText = Color(errorText),
    priorityHighSurface = Color(priorityHighSurface),
    priorityHighText = Color(priorityHighText),
    priorityMedSurface = Color(priorityMedSurface),
    priorityMedText = Color(priorityMedText),
    priorityLowSurface = Color(priorityLowSurface),
    priorityLowText = Color(priorityLowText),
    developmentSurface = Color(developmentSurface),
    developmentText = Color(developmentText),
    preProductionSurface = Color(preProductionSurface),
    preProductionText = Color(preProductionText),
    productionSurface = Color(productionSurface),
    productionText = Color(productionText),
    postProductionSurface = Color(postProductionSurface),
    postProductionText = Color(postProductionText),
    distributionSurface = Color(distributionSurface),
    distributionText = Color(distributionText)
  )

private fun TypographyTokens.toTypographySystem() =
  TypographySystem(
    displayLarge = displayLarge.toTextStyle(),
    displayMedium = displayMedium.toTextStyle(),
    titleLarge = titleLarge.toTextStyle(),
    titleExtraLarge = titleExtraLarge.toTextStyle(),
    titleMedium = titleMedium.toTextStyle(),
    titleSmall = titleSmall.toTextStyle(),
    bodyLarge = bodyLarge.toTextStyle(),
    bodyMedium = bodyMedium.toTextStyle(),
    bodySmall = bodySmall.toTextStyle(),
    labelLarge = labelLarge.toTextStyle(),
    labelMedium = labelMedium.toTextStyle(),
    labelSmall = labelSmall.toTextStyle(),
    caption = caption.toTextStyle(),
    monoMedium = monoMedium.toTextStyle(),
    monoSmall = monoSmall.toTextStyle()
  )

private fun TextStyleToken.toTextStyle() =
  TextStyle(
    fontFamily = when (fontFamily) {
      FontFamilyToken.SANS -> FontFamily.SansSerif
      FontFamilyToken.MONO -> FontFamily.Monospace
    },
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    letterSpacing = letterSpacing?.sp ?: TextUnit.Unspecified,
    lineHeight = lineHeight?.sp ?: TextUnit.Unspecified
  )

private fun CubicBezier.toEasing(): Easing =
  if (this == CubicBezier.Linear) LinearEasing else CubicBezierEasing(x1, y1, x2, y2)
