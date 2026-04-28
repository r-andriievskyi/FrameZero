package com.discovery.playground.shared.design_system

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontFamily
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
import com.discovery.playground.shared.design_system.tokens.TokenRadiusButton
import com.discovery.playground.shared.design_system.tokens.TokenRadiusCard
import com.discovery.playground.shared.design_system.tokens.TokenRadiusCircle
import com.discovery.playground.shared.design_system.tokens.TokenRadiusInput
import com.discovery.playground.shared.design_system.tokens.TokenRadiusLg
import com.discovery.playground.shared.design_system.tokens.TokenRadiusMd
import com.discovery.playground.shared.design_system.tokens.TokenRadiusSegItem
import com.discovery.playground.shared.design_system.tokens.TokenRadiusSheet
import com.discovery.playground.shared.design_system.tokens.TokenRadiusSm
import com.discovery.playground.shared.design_system.tokens.TokenRadiusXs
import com.discovery.playground.shared.design_system.tokens.TokenSpaceLg
import com.discovery.playground.shared.design_system.tokens.TokenSpaceMd
import com.discovery.playground.shared.design_system.tokens.TokenSpaceSm
import com.discovery.playground.shared.design_system.tokens.TokenSpaceX3l
import com.discovery.playground.shared.design_system.tokens.TokenSpaceX4l
import com.discovery.playground.shared.design_system.tokens.TokenSpaceX5l
import com.discovery.playground.shared.design_system.tokens.TokenSpaceX6l
import com.discovery.playground.shared.design_system.tokens.TokenSpaceXl
import com.discovery.playground.shared.design_system.tokens.TokenSpaceXs
import com.discovery.playground.shared.design_system.tokens.TokenSpaceXxl
import com.discovery.playground.shared.design_system.tokens.TokenSpaceXxs
import com.discovery.playground.shared.design_system.tokens.fontBodySmall
import com.discovery.playground.shared.design_system.tokens.fontBodyStandard
import com.discovery.playground.shared.design_system.tokens.fontButton
import com.discovery.playground.shared.design_system.tokens.fontLabel
import com.discovery.playground.shared.design_system.tokens.fontTitleSection

@Immutable
data class ThemeOptions(
  val colorSystem: ColorSystem,
  val typographySystem: TypographySystem,
  val spacingSystem: SpacingSystem,
  val radiusSystem: RadiusSystem,
) {
  companion object {
    fun light(fontFamily: FontFamily) =
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
        typographySystem = sharedTypography(fontFamily),
        spacingSystem = sharedSpacing(),
        radiusSystem = sharedRadius(),
      )

    fun dark(fontFamily: FontFamily) =
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
        typographySystem = sharedTypography(fontFamily),
        spacingSystem = sharedSpacing(),
        radiusSystem = sharedRadius(),
      )

    private fun sharedTypography(fontFamily: FontFamily) =
      TypographySystem(
        titleSection = fontTitleSection(fontFamily),
        bodyStandard = fontBodyStandard(fontFamily),
        bodySmall = fontBodySmall(fontFamily),
        label = fontLabel(fontFamily),
        button = fontButton(fontFamily),
      )

    private fun sharedSpacing() =
      SpacingSystem(
        xxs = TokenSpaceXxs,
        xs = TokenSpaceXs,
        sm = TokenSpaceSm,
        md = TokenSpaceMd,
        lg = TokenSpaceLg,
        xl = TokenSpaceXl,
        xxl = TokenSpaceXxl,
        x3l = TokenSpaceX3l,
        x4l = TokenSpaceX4l,
        x5l = TokenSpaceX5l,
        x6l = TokenSpaceX6l,
      )

    private fun sharedRadius() =
      RadiusSystem(
        xs = TokenRadiusXs,
        sm = TokenRadiusSm,
        segItem = TokenRadiusSegItem,
        md = TokenRadiusMd,
        lg = TokenRadiusLg,
        input = TokenRadiusInput,
        button = TokenRadiusButton,
        card = TokenRadiusCard,
        sheet = TokenRadiusSheet,
        circle = TokenRadiusCircle,
      )
  }
}
