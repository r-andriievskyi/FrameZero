package com.frame.zero.feature.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import framezero.composeapp.generated.resources.Res
import framezero.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

private val SplashItemSpacing = 24.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SplashContent() {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(SplashItemSpacing)
    ) {
      Text(text = stringResource(Res.string.app_name), style = AppTheme.typographySystem.displayMedium)
      LoadingIndicator(color = AppTheme.colorSystem.accent)
    }
  }
}
