package com.frame.zero

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.frame.zero.feature.RootComponent
import com.frame.zero.feature.account.ui.AccountScreen
import com.frame.zero.feature.auth.ui.AuthContent
import com.frame.zero.feature.home.ui.HomeContent
import com.frame.zero.feature.production.details.ui.ProductionDetailsScreen
import com.frame.zero.feature.production.ui.CreateProductionScreen
import com.frame.zero.feature.splash.SplashContent
import com.frame.zero.feature.task.details.ui.TaskDetailsScreen

@Composable
fun RootContent(component: RootComponent) {
  Children(
    stack = component.stack,
    animation = stackAnimation()
  ) { child ->
    when (val instance = child.instance) {
      RootComponent.Child.Splash -> SplashContent()
      is RootComponent.Child.Auth -> AuthContent(instance.component)
      is RootComponent.Child.Home -> HomeContent(instance.component)
      is RootComponent.Child.Account -> AccountScreen(instance.component)
      is RootComponent.Child.CreateProduction -> CreateProductionScreen(instance.component)
      is RootComponent.Child.ProductionDetails -> ProductionDetailsScreen(instance.component)
      is RootComponent.Child.TaskDetails -> TaskDetailsScreen(instance.component)
    }
  }
}
