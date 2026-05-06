package com.frame.zero

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.frame.zero.feature.RootComponent
import com.frame.zero.feature.auth.ui.AuthContent
import com.frame.zero.feature.home.ui.HomeContent
import com.frame.zero.feature.production.ui.CreateProductionContent
import com.frame.zero.feature.splash.SplashContent

@Composable
fun RootContent(component: RootComponent) {
  Children(stack = component.stack) { child ->
    when (val instance = child.instance) {
      RootComponent.Child.Splash -> SplashContent()
      is RootComponent.Child.Auth -> AuthContent(instance.component)
      is RootComponent.Child.Home -> HomeContent(instance.component)
      is RootComponent.Child.CreateProduction -> CreateProductionContent(instance.component)
    }
  }
}
