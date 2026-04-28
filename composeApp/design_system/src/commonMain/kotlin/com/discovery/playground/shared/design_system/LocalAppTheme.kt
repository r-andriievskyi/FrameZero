package com.discovery.playground.shared.design_system

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import com.discovery.playground.shared.design_system.tokens.rememberFontFamilyPrimary

private val LocalAppTheme =
  compositionLocalOf<ThemeOptions> {
    error("AppTheme not provided. Wrap your root composable with AppTheme { }.")
  }

object AppTheme {
  @Composable
  operator fun invoke(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
  ) {
    val fontFamily = rememberFontFamilyPrimary()
    val themeOptions =
      remember(darkTheme, fontFamily) {
        if (darkTheme) ThemeOptions.dark(fontFamily) else ThemeOptions.light(fontFamily)
      }
    ApplySystemUiColors(darkTheme)
    CompositionLocalProvider(LocalAppTheme provides themeOptions) { content() }
  }

  val colorSystem: ColorSystem
    @Composable get() = LocalAppTheme.current.colorSystem

  val typographySystem: TypographySystem
    @Composable get() = LocalAppTheme.current.typographySystem

  val spacingSystem: SpacingSystem
    @Composable get() = LocalAppTheme.current.spacingSystem

  val radiusSystem: RadiusSystem
    @Composable get() = LocalAppTheme.current.radiusSystem
}
