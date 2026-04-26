package com.frame.zero.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.frame.zero.feature.dashboard.DashboardComponent

@Composable
fun DashboardContent(@Suppress("UNUSED_PARAMETER") component: DashboardComponent) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = "Dashboard", style = MaterialTheme.typography.headlineLarge)
  }
}
