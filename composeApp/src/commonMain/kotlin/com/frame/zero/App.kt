package com.frame.zero

import androidx.compose.runtime.Composable
import com.discovery.playground.shared.design_system.AppTheme
import com.frame.zero.feature.RootComponent

@Composable
fun App(root: RootComponent) {
  AppTheme { RootContent(root) }
}
