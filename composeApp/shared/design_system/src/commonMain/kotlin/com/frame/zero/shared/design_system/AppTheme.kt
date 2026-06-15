package com.frame.zero.shared.design_system

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember

internal val LocalAppTheme = compositionLocalOf<ThemeOptions> {
  error("AppTheme not provided. Wrap your root composable with AppTheme { }.")
}

object AppTheme {
  @Composable
  operator fun invoke(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
  ) {
    val themeOptions =
      remember(darkTheme) { if (darkTheme) ThemeOptions.dark() else ThemeOptions.light() }
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

  val borderSystem: BorderSystem
    @Composable get() = LocalAppTheme.current.borderSystem
}
