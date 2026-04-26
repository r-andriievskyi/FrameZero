package com.frame.zero.feature

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.frame.zero.di.platformModule
import com.frame.zero.di.sharedModule
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.auth.AuthViewModel
import com.frame.zero.feature.dashboard.DashboardComponent
import org.koin.core.Koin
import org.koin.core.context.startKoin

class RootComponent(
  componentContext: ComponentContext,
  private val authViewModelFactory: () -> AuthViewModel,
) : ComponentContext by componentContext {

  private val navigation = StackNavigation<Config>()

  val stack: Value<ChildStack<Config, Child>> =
    childStack(
      source = navigation,
      serializer = null,
      initialConfiguration = Config.Auth,
      handleBackButton = true,
      childFactory = ::createChild,
    )

  private fun createChild(config: Config, context: ComponentContext): Child =
    when (config) {
      Config.Auth ->
        Child.Auth(
          AuthComponent(
            componentContext = context,
            onAuthenticated = { navigation.replaceAll(Config.Dashboard) },
            viewModel = authViewModelFactory(),
          )
        )
      Config.Dashboard -> Child.Dashboard(DashboardComponent(context))
    }

  sealed interface Config {
    data object Auth : Config

    data object Dashboard : Config
  }

  sealed interface Child {
    data class Auth(val component: AuthComponent) : Child

    data class Dashboard(val component: DashboardComponent) : Child
  }
}

fun initKoin(): Koin = startKoin { modules(sharedModule, platformModule()) }.koin
