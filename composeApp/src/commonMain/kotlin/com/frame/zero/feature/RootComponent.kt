package com.frame.zero.feature

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.SessionState
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.home.HomeComponent
import com.frame.zero.feature.production.CreateProductionComponent
import com.frame.zero.feature.production.CreateProductionViewModel
import com.frame.zero.feature.production.details.ProductionDetailsComponent
import com.frame.zero.feature.production.details.ProductionDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class RootComponent(
  componentContext: ComponentContext,
  sessionManager: SessionManager,
  private val authComponentFactory: (ComponentContext) -> AuthComponent,
  private val homeComponentFactory: (
    ComponentContext,
    onCreateProductionClick: () -> Unit,
    onProductionClick: (productionId: String) -> Unit
  ) -> HomeComponent,
  private val createProductionViewModelFactory: () -> CreateProductionViewModel,
  private val productionDetailsViewModelFactory: (productionId: String) -> ProductionDetailsViewModel
) : ComponentContext by componentContext {
  private val navigation = StackNavigation<Config>()

  val stack: Value<ChildStack<Config, Child>> =
    childStack(
      source = navigation,
      serializer = null,
      initialConfiguration = Config.Splash,
      handleBackButton = true,
      childFactory = ::createChild
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
            is SessionState.LoggedIn -> Config.Home
          }
        if (stack.value.active.configuration != target) navigation.replaceAll(target)
      }
    }
  }

  private fun createChild(
    config: Config,
    context: ComponentContext
  ): Child =
    when (config) {
      Config.Splash -> Child.Splash
      Config.Auth -> Child.Auth(authComponentFactory(context))
      Config.Home -> Child.Home(
        homeComponentFactory(
          context,
          {
            @OptIn(DelicateDecomposeApi::class)
            navigation.push(Config.CreateProduction)
          },
          { productionId ->
            @OptIn(DelicateDecomposeApi::class)
            navigation.push(Config.ProductionDetails(productionId))
          }
        )
      )
      Config.CreateProduction -> Child.CreateProduction(
        CreateProductionComponent(
          componentContext = context,
          onBack = { navigation.pop() },
          onCreated = { navigation.pop() },
          viewModelFactory = createProductionViewModelFactory
        )
      )
      is Config.ProductionDetails -> Child.ProductionDetails(
        ProductionDetailsComponent(
          componentContext = context,
          productionId = config.productionId,
          onBack = { navigation.pop() },
          viewModelFactory = productionDetailsViewModelFactory
        )
      )
    }

  sealed interface Config {
    data object Splash : Config

    data object Auth : Config

    data object Home : Config

    data object CreateProduction : Config

    data class ProductionDetails(
      val productionId: String
    ) : Config
  }

  sealed interface Child {
    data object Splash : Child

    data class Auth(
      val component: AuthComponent
    ) : Child

    data class Home(
      val component: HomeComponent
    ) : Child

    data class CreateProduction(
      val component: CreateProductionComponent
    ) : Child

    data class ProductionDetails(
      val component: ProductionDetailsComponent
    ) : Child
  }
}
