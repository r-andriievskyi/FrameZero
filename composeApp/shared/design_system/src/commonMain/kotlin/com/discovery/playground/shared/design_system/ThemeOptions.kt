package com.discovery.playground.shared.design_system

import androidx.compose.runtime.Immutable
import com.discovery.playground.shared.design_system.tokens.BodyLarge
import com.discovery.playground.shared.design_system.tokens.BodyMedium
import com.discovery.playground.shared.design_system.tokens.BodySmall
import com.discovery.playground.shared.design_system.tokens.Caption
import com.discovery.playground.shared.design_system.tokens.DisplayLarge
import com.discovery.playground.shared.design_system.tokens.DisplayMedium
import com.discovery.playground.shared.design_system.tokens.LabelLarge
import com.discovery.playground.shared.design_system.tokens.LabelMedium
import com.discovery.playground.shared.design_system.tokens.LabelSmall
import com.discovery.playground.shared.design_system.tokens.MonoMedium
import com.discovery.playground.shared.design_system.tokens.MonoSmall
import com.discovery.playground.shared.design_system.tokens.TitleLarge
import com.discovery.playground.shared.design_system.tokens.TitleMedium
import com.discovery.playground.shared.design_system.tokens.TitleSmall
import com.discovery.playground.shared.design_system.tokens.TokenColorAmberSoftD
import com.discovery.playground.shared.design_system.tokens.TokenColorAmberSoftL
import com.discovery.playground.shared.design_system.tokens.TokenColorAmberTextD
import com.discovery.playground.shared.design_system.tokens.TokenColorAmberTextL
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral050
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral100
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral100L
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral200L
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral300
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral400
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral500L
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral600
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral600L
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral700
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral750
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral780
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral800
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral850
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral900
import com.discovery.playground.shared.design_system.tokens.TokenColorNeutral900L
import com.discovery.playground.shared.design_system.tokens.TokenColorRoseSoftD
import com.discovery.playground.shared.design_system.tokens.TokenColorRoseSoftL
import com.discovery.playground.shared.design_system.tokens.TokenColorRoseTextD
import com.discovery.playground.shared.design_system.tokens.TokenColorRoseTextL
import com.discovery.playground.shared.design_system.tokens.TokenColorTealSoftD
import com.discovery.playground.shared.design_system.tokens.TokenColorTealSoftL
import com.discovery.playground.shared.design_system.tokens.TokenColorTealTextD
import com.discovery.playground.shared.design_system.tokens.TokenColorTealTextL
import com.discovery.playground.shared.design_system.tokens.TokenColorViolet500
import com.discovery.playground.shared.design_system.tokens.TokenColorViolet600
import com.discovery.playground.shared.design_system.tokens.TokenColorVioletSoftD
import com.discovery.playground.shared.design_system.tokens.TokenColorVioletSoftL
import com.discovery.playground.shared.design_system.tokens.TokenColorVioletTextD
import com.discovery.playground.shared.design_system.tokens.TokenColorWhite
import com.discovery.playground.shared.design_system.tokens.TokenRadius16
import com.discovery.playground.shared.design_system.tokens.TokenRadius4
import com.discovery.playground.shared.design_system.tokens.TokenRadius8
import com.discovery.playground.shared.design_system.tokens.TokenRadiusMax
import com.discovery.playground.shared.design_system.tokens.TokenSpace16
import com.discovery.playground.shared.design_system.tokens.TokenSpace2
import com.discovery.playground.shared.design_system.tokens.TokenSpace24
import com.discovery.playground.shared.design_system.tokens.TokenSpace4
import com.discovery.playground.shared.design_system.tokens.TokenSpace8

@Immutable
data class ThemeOptions(
  val colorSystem: ColorSystem,
  val typographySystem: TypographySystem,
  val spacingSystem: SpacingSystem,
  val radiusSystem: RadiusSystem,
) {
  companion object {
    fun light() =
      ThemeOptions(
        colorSystem =
          ColorSystem(
            background = TokenColorNeutral050,
            surfaceElevated = TokenColorNeutral100L,
            navBackground = TokenColorNeutral050,
            inputBackground = TokenColorNeutral100L,
            cardBackground = TokenColorNeutral100L,
            border = TokenColorNeutral200L,
            cardBorder = TokenColorNeutral200L,
            textPrimary = TokenColorNeutral900L,
            textSecondary = TokenColorNeutral600L,
            textMuted = TokenColorNeutral500L,
            textOnAccent = TokenColorWhite,
            accent = TokenColorViolet500,
            accentDim = TokenColorViolet600,
            accentSurface = TokenColorVioletSoftL,
            accentText = TokenColorViolet500,
            successSurface = TokenColorTealSoftL,
            successText = TokenColorTealTextL,
            warningSurface = TokenColorAmberSoftL,
            warningText = TokenColorAmberTextL,
            errorSurface = TokenColorRoseSoftL,
            errorText = TokenColorRoseTextL,
            priorityHighSurface = TokenColorRoseSoftL,
            priorityHighText = TokenColorRoseTextL,
            priorityMedSurface = TokenColorAmberSoftL,
            priorityMedText = TokenColorAmberTextL,
            priorityLowSurface = TokenColorTealSoftL,
            priorityLowText = TokenColorTealTextL,
          ),
        typographySystem = sharedTypography(),
        spacingSystem = sharedSpacing(),
        radiusSystem = sharedRadius(),
      )

    fun dark() =
      ThemeOptions(
        colorSystem =
          ColorSystem(
            background = TokenColorNeutral900,
            surfaceElevated = TokenColorNeutral750,
            navBackground = TokenColorNeutral850,
            inputBackground = TokenColorNeutral800,
            cardBackground = TokenColorNeutral780,
            border = TokenColorNeutral600,
            cardBorder = TokenColorNeutral700,
            textPrimary = TokenColorNeutral100,
            textSecondary = TokenColorNeutral300,
            textMuted = TokenColorNeutral400,
            textOnAccent = TokenColorWhite,
            accent = TokenColorViolet500,
            accentDim = TokenColorViolet600,
            accentSurface = TokenColorVioletSoftD,
            accentText = TokenColorVioletTextD,
            successSurface = TokenColorTealSoftD,
            successText = TokenColorTealTextD,
            warningSurface = TokenColorAmberSoftD,
            warningText = TokenColorAmberTextD,
            errorSurface = TokenColorRoseSoftD,
            errorText = TokenColorRoseTextD,
            priorityHighSurface = TokenColorRoseSoftD,
            priorityHighText = TokenColorRoseTextD,
            priorityMedSurface = TokenColorAmberSoftD,
            priorityMedText = TokenColorAmberTextD,
            priorityLowSurface = TokenColorTealSoftD,
            priorityLowText = TokenColorTealTextD,
          ),
        typographySystem = sharedTypography(),
        spacingSystem = sharedSpacing(),
        radiusSystem = sharedRadius(),
      )

    private fun sharedTypography() =
      TypographySystem(
        displayLarge = DisplayLarge,
        displayMedium = DisplayMedium,
        titleLarge = TitleLarge,
        titleMedium = TitleMedium,
        titleSmall = TitleSmall,
        bodyLarge = BodyLarge,
        bodyMedium = BodyMedium,
        bodySmall = BodySmall,
        labelLarge = LabelLarge,
        labelMedium = LabelMedium,
        labelSmall = LabelSmall,
        caption = Caption,
        monoMedium = MonoMedium,
        monoSmall = MonoSmall,
      )

    private fun sharedSpacing() =
      SpacingSystem(
        space2 = TokenSpace2,
        space4 = TokenSpace4,
        space8 = TokenSpace8,
        space16 = TokenSpace16,
        space24 = TokenSpace24)

    private fun sharedRadius() =
      RadiusSystem(
        radius4 = TokenRadius4,
        radius8 = TokenRadius8,
        radius16 = TokenRadius16,
        radiusMax = TokenRadiusMax)
  }
}
