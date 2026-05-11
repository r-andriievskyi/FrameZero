package com.frame.zero.feature.account.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.discovery.playground.shared.design_system.AppTheme
import com.frame.zero.feature.account.AccountComponent

@Composable
fun AccountContent(component: AccountComponent, modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize())
}

@Preview
@Composable
private fun AccountContentPreview() {
  AppTheme(darkTheme = true) {
    Box(modifier = Modifier.fillMaxSize())
  }
}
