package com.frame.zero.feature.home.ui.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.frame.zero.feature.home.tab.dashboard.DashboardTabComponent

/**
 * Empty stub. Real UI to come later — render `component.state.collectAsState()` then.
 *
 * The `LaunchedEffect(Unit)` is the load trigger: it fires the first time this page composes. With
 * the pager's `beyondViewportPageCount = 1`, that happens for the active tab plus its immediate
 * neighbor on first render — that's the preload window.
 */
@Composable
fun DashboardTabContent(component: DashboardTabComponent) {
  LaunchedEffect(Unit) { component.onAppeared() }
  Box(modifier = Modifier.fillMaxSize().background(Color.Blue))
}
