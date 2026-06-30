package com.frame.zero.feature.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme

/**
 * The Android OS splash screen (held via `setKeepOnScreenCondition`) covers the
 * `Loading` window, so the in-app splash only needs to paint the background.
 */
@Composable
actual fun PlatformSplash() {
  Box(modifier = Modifier.fillMaxSize().background(AppTheme.colorSystem.background))
}
