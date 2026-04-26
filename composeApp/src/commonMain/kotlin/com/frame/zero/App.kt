package com.frame.zero

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.frame.zero.feature.RootComponent

@Composable
fun App(root: RootComponent) {
  MaterialTheme { RootContent(root) }
}
