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
import com.discovery.playground.shared.design_system.tokens.TokenColorBeacon500
import com.discovery.playground.shared.design_system.tokens.TokenColorBeacon600
import com.discovery.playground.shared.design_system.tokens.TokenColorBeaconSoftD
import com.discovery.playground.shared.design_system.tokens.TokenColorBeaconSoftL
import com.discovery.playground.shared.design_system.tokens.TokenColorBeaconTextD
import com.discovery.playground.shared.design_system.tokens.TokenColorBeaconTextL
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
import com.discovery.playground.shared.design_system.tokens.amberSoftD
import com.discovery.playground.shared.design_system.tokens.amberSoftL
import com.discovery.playground.shared.design_system.tokens.amberTextD
import com.discovery.playground.shared.design_system.tokens.amberTextL
import com.discovery.playground.shared.design_system.tokens.beaconSoftD
import com.discovery.playground.shared.design_system.tokens.beaconSoftL
import com.discovery.playground.shared.design_system.tokens.beaconTextD
import com.discovery.playground.shared.design_system.tokens.beaconTextL
import com.discovery.playground.shared.design_system.tokens.roseSoftD
import com.discovery.playground.shared.design_system.tokens.roseSoftL
import com.discovery.playground.shared.design_system.tokens.roseTextD
import com.discovery.playground.shared.design_system.tokens.roseTextL
import com.discovery.playground.shared.design_system.tokens.skySoftD
import com.discovery.playground.shared.design_system.tokens.skySoftL
import com.discovery.playground.shared.design_system.tokens.skyTextD
import com.discovery.playground.shared.design_system.tokens.skyTextL
import com.discovery.playground.shared.design_system.tokens.tealSoftD
import com.discovery.playground.shared.design_system.tokens.tealSoftL
import com.discovery.playground.shared.design_system.tokens.tealTextD
import com.discovery.playground.shared.design_system.tokens.tealTextL

@Immutable
data class ThemeOptions(
  val colorSystem: ColorSystem,
  val typographySystem: TypographySystem,
  val spacingSystem: SpacingSystem,
  val radiusSystem: RadiusSystem
) {
  companion object {
    fun light() = ThemeOptions(
      colorSystem = ColorSystem(
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
        accent = TokenColorBeacon500,
        accentDim = TokenColorBeacon600,
        accentSurface = TokenColorBeaconSoftL,
        accentText = TokenColorBeaconTextL,
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
        developmentSurface = skySoftL,
        developmentText = skyTextL,
        preProductionSurface = amberSoftL,
        preProductionText = amberTextL,
        productionSurface = tealSoftL,
        productionText = tealTextL,
        postProductionSurface = beaconSoftL,
        postProductionText = beaconTextL,
        distributionSurface = roseSoftL,
        distributionText = roseTextL
      ),
      typographySystem = sharedTypography(),
      spacingSystem = sharedSpacing(),
      radiusSystem = sharedRadius()
    )

    fun dark() = ThemeOptions(
      colorSystem = ColorSystem(
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
        accent = TokenColorBeacon500,
        accentDim = TokenColorBeacon600,
        accentSurface = TokenColorBeaconSoftD,
        accentText = TokenColorBeaconTextD,
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
        developmentSurface = skySoftD,
        developmentText = skyTextD,
        preProductionSurface = amberSoftD,
        preProductionText = amberTextD,
        productionSurface = tealSoftD,
        productionText = tealTextD,
        postProductionSurface = beaconSoftD,
        postProductionText = beaconTextD,
        distributionSurface = roseSoftD,
        distributionText = roseTextD,
      ),
      typographySystem = sharedTypography(),
      spacingSystem = sharedSpacing(),
      radiusSystem = sharedRadius()
    )

    private fun sharedTypography() = TypographySystem(
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
      monoSmall = MonoSmall
    )

    private fun sharedSpacing() = SpacingSystem(
      space2 = TokenSpace2,
      space4 = TokenSpace4,
      space8 = TokenSpace8,
      space16 = TokenSpace16,
      space24 = TokenSpace24
    )

    private fun sharedRadius() = RadiusSystem(
      radius4 = TokenRadius4,
      radius8 = TokenRadius8,
      radius16 = TokenRadius16,
      radiusMax = TokenRadiusMax
    )
  }
}
