package com.frame.zero

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.frame.zero.auth.AuthContent
import com.frame.zero.dashboard.DashboardContent
import com.frame.zero.feature.RootComponent

@Composable
fun RootContent(component: RootComponent) {
  Children(stack = component.stack) { child ->
    when (val instance = child.instance) {
      is RootComponent.Child.Auth -> AuthContent(instance.component)
      is RootComponent.Child.Dashboard -> DashboardContent(instance.component)
    }
  }
}
