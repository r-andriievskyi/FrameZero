package com.frame.zero.feature

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.SessionState
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.dashboard.DashboardComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class RootComponent(
  componentContext: ComponentContext,
  sessionManager: SessionManager,
  private val authComponentFactory: (ComponentContext) -> AuthComponent,
  private val dashboardComponentFactory: (ComponentContext) -> DashboardComponent,
) : ComponentContext by componentContext {

  private val navigation = StackNavigation<Config>()

  val stack: Value<ChildStack<Config, Child>> =
    childStack(
      source = navigation,
      serializer = null,
      initialConfiguration = Config.Splash,
      handleBackButton = false,
      childFactory = ::createChild,
    )

  init {
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    lifecycle.doOnDestroy { scope.cancel() }
    scope.launch {
      sessionManager.state.collect { sessionState ->
        val target =
          when (sessionState) {
            SessionState.Loading -> Config.Splash
            SessionState.LoggedOut -> Config.Auth
            is SessionState.LoggedIn -> Config.Dashboard
          }
        if (stack.value.active.configuration != target) navigation.replaceAll(target)
      }
    }
  }

  private fun createChild(config: Config, context: ComponentContext): Child =
    when (config) {
      Config.Splash -> Child.Splash
      Config.Auth -> Child.Auth(authComponentFactory(context))
      Config.Dashboard -> Child.Dashboard(dashboardComponentFactory(context))
    }

  sealed interface Config {
    data object Splash : Config

    data object Auth : Config

    data object Dashboard : Config
  }

  sealed interface Child {
    data object Splash : Child

    data class Auth(val component: AuthComponent) : Child

    data class Dashboard(val component: DashboardComponent) : Child
  }
}
