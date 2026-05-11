package com.frame.zero.feature.production.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.discovery.playground.shared.design_system.AppTheme
import com.frame.zero.feature.production.details.ProductionDetailsComponent
import com.frame.zero.feature.production.details.ProductionDetailsState

@Composable
fun ProductionDetailsContent(component: ProductionDetailsComponent) {
  val state by component.state.collectAsState()
  ProductionDetailsScreen(state = state)
}

@Composable
internal fun ProductionDetailsScreen(
  @Suppress("UnusedParameter") state: ProductionDetailsState,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .systemBarsPadding()
  )
}

@Preview
@Composable
private fun ProductionDetailsPreview() {
  AppTheme(darkTheme = true) {
    ProductionDetailsScreen(state = ProductionDetailsState())
  }
}
