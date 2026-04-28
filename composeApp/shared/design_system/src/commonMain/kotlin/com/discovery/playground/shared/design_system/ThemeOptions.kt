package com.discovery.playground.shared.design_system

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontFamily
import com.discovery.playground.shared.design_system.tokens.TokenColorBlack
import com.discovery.playground.shared.design_system.tokens.TokenColorErrorDark
import com.discovery.playground.shared.design_system.tokens.TokenColorErrorLight
import com.discovery.playground.shared.design_system.tokens.TokenColorGreen400
import com.discovery.playground.shared.design_system.tokens.TokenColorGreen500
import com.discovery.playground.shared.design_system.tokens.TokenColorGreen800
import com.discovery.playground.shared.design_system.tokens.TokenColorGreen950
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
            background = TokenColorWhite,
            secondary = TokenColorBlack,
            primary = TokenColorGreen800,
            accent = TokenColorGreen500,
            error = TokenColorErrorLight,
          ),
        typographySystem = sharedTypography(fontFamily),
        spacingSystem = sharedSpacing(),
        radiusSystem = sharedRadius(),
      )

    fun dark(fontFamily: FontFamily) =
      ThemeOptions(
        colorSystem =
          ColorSystem(
            background = TokenColorGreen950,
            secondary = TokenColorWhite,
            primary = TokenColorGreen800,
            accent = TokenColorGreen400,
            error = TokenColorErrorDark,
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
        space2 = TokenSpace2,
        space4 = TokenSpace4,
        space8 = TokenSpace8,
        space16 = TokenSpace16,
        space24 = TokenSpace24,
      )

    private fun sharedRadius() =
      RadiusSystem(
        radius4 = TokenRadius4,
        radius8 = TokenRadius8,
        radius16 = TokenRadius16,
        radiusMax = TokenRadiusMax,
      )
  }
}
