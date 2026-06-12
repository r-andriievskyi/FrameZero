package com.frame.zero.shared.design_system.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FullScreenProgress(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    LoadingIndicator(color = AppTheme.colorSystem.accent)
  }
}

@LightDarkPreview
@Composable
private fun FullScreenProgressPreview() {
  AppTheme {
    Box(modifier = Modifier.fillMaxSize().background(AppTheme.colorSystem.background)) {
      FullScreenProgress()
    }
  }
}
