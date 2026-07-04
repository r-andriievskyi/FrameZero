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
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.frame.zero.core.navigation.DeepLink
import com.frame.zero.core.navigation.NavigationSignal
import com.frame.zero.core.security.AppLockController
import com.frame.zero.core.security.AppLockState
import com.frame.zero.core.security.BiometricPromptText
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.SessionState
import com.frame.zero.feature.account.AccountComponent
import com.frame.zero.feature.account.AccountViewModel
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.chat.ChatComponent
import com.frame.zero.feature.chat.ChatViewModel
import com.frame.zero.feature.home.HomeComponent
import com.frame.zero.feature.production.CreateProductionComponent
import com.frame.zero.feature.production.CreateProductionViewModel
import com.frame.zero.feature.production.details.ProductionDetailsComponent
import com.frame.zero.feature.production.details.ProductionDetailsViewModel
import com.frame.zero.feature.task.create.CreateTaskComponent
import com.frame.zero.feature.task.create.CreateTaskViewModel
import com.frame.zero.feature.task.details.TaskDetailsComponent
import com.frame.zero.feature.task.details.TaskDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class RootComponent(
  componentContext: ComponentContext,
  private val sessionManager: SessionManager,
  private val appLockController: AppLockController,
  navigationSignal: NavigationSignal,
  private val authComponentFactory: (ComponentContext) -> AuthComponent,
  private val homeComponentFactory: (
    ComponentContext,
    onCreateProductionClick: () -> Unit,
    onProductionClick: (productionId: String) -> Unit,
    onAccountClick: () -> Unit,
    onTaskClick: (taskId: String) -> Unit
  ) -> HomeComponent,
  private val createProductionViewModelFactory: () -> CreateProductionViewModel,
  private val productionDetailsViewModelFactory: (productionId: String) -> ProductionDetailsViewModel,
  private val taskDetailsViewModelFactory: (taskId: String) -> TaskDetailsViewModel,
  private val createTaskViewModelFactory: (
    productionId: String,
    productionTitle: String
  ) -> CreateTaskViewModel,
  private val chatViewModelFactory: (taskId: String) -> ChatViewModel,
  private val accountViewModelFactory: () -> AccountViewModel
) : ComponentContext by componentContext {
  private val navigation = StackNavigation<Config>()
  private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  val stack: Value<ChildStack<Config, Child>> =
    childStack(
      source = navigation,
      serializer = Config.serializer(),
      initialConfiguration = Config.Splash,
      handleBackButton = true,
      childFactory = ::createChild
    )

  /**
   * The biometric lock overlay is shown only when the user is signed in *and* the lock
   * is engaged — a locked session at the auth screen would make no sense. Combines the
   * session and lock state so the UI observes a single boolean.
   */
  val isLocked: StateFlow<Boolean> = combine(
    sessionManager.state,
    appLockController.lockState
  ) { session, lock ->
    session is SessionState.LoggedIn && lock == AppLockState.Locked
  }.stateIn(scope, SharingStarted.Eagerly, false)

  // Swallows the system back button while the lock overlay is up so back can't drive the
  // covered navigation stack (pop a hidden screen, or exit the app) during a locked session.
  // Registered after the child stack, so it takes priority when enabled.
  private val lockBackCallback = BackCallback(isEnabled = false) {}

  init {
    backHandler.register(lockBackCallback)
    lifecycle.doOnDestroy { scope.cancel() }
    scope.launch { isLocked.collect { locked -> lockBackCallback.isEnabled = locked } }
    scope.launch {
      sessionManager.state.collect { sessionState ->
        val target = when (sessionState) {
          SessionState.Loading -> Config.Splash
          SessionState.LoggedOut -> Config.Auth
          is SessionState.LoggedIn -> Config.Home
        }
        if (stack.value.active.configuration != target) navigation.replaceAll(target)
      }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    scope.launch {
      sessionManager.state
        .filterIsInstance<SessionState.LoggedIn>()
        .flatMapLatest { navigationSignal.events }
        .collect { deepLink ->
          navigate(deepLink)
          navigationSignal.consume()
        }
    }
  }

  fun unlock(prompt: BiometricPromptText) {
    scope.launch { appLockController.authenticate(prompt) }
  }

  fun onLockSignOut() {
    scope.launch { sessionManager.logout() }
  }

  private fun navigate(deepLink: DeepLink) {
    val config = when (deepLink) {
      is DeepLink.TaskDetails -> Config.TaskDetails(deepLink.taskId)
    }
    @OptIn(DelicateDecomposeApi::class)
    navigation.push(config)
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
          },
          {
            @OptIn(DelicateDecomposeApi::class)
            navigation.push(Config.Account)
          },
          { taskId ->
            @OptIn(DelicateDecomposeApi::class)
            navigation.push(Config.TaskDetails(taskId))
          }
        )
      )

      Config.Account -> Child.Account(
        AccountComponent(
          componentContext = context,
          onBack = { navigation.pop() },
          onEditProfile = { /* TODO: navigate to edit profile */ },
          onEmailSettings = { /* TODO: navigate to email settings */ },
          onPasswordSecurity = { /* TODO: navigate to password & security */ },
          onNotifications = { /* TODO: navigate to notification settings */ },
          viewModelFactory = accountViewModelFactory
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
          onDeleted = { navigation.pop() },
          onAddTask = { productionId, productionTitle ->
            @OptIn(DelicateDecomposeApi::class)
            navigation.push(
              Config.CreateTask(
                productionId = productionId,
                productionTitle = productionTitle
              )
            )
          },
          viewModelFactory = productionDetailsViewModelFactory
        )
      )

      is Config.TaskDetails -> Child.TaskDetails(
        TaskDetailsComponent(
          componentContext = context,
          taskId = config.taskId,
          onBack = { navigation.pop() },
          onOpenChat = {
            @OptIn(DelicateDecomposeApi::class)
            navigation.push(Config.Chat(config.taskId))
          },
          viewModelFactory = taskDetailsViewModelFactory
        )
      )

      is Config.Chat -> Child.Chat(
        ChatComponent(
          componentContext = context,
          taskId = config.taskId,
          onBack = { navigation.pop() },
          viewModelFactory = chatViewModelFactory
        )
      )

      is Config.CreateTask -> Child.CreateTask(
        CreateTaskComponent(
          componentContext = context,
          productionId = config.productionId,
          productionTitle = config.productionTitle,
          onBack = { navigation.pop() },
          onCreated = { navigation.pop() },
          viewModelFactory = createTaskViewModelFactory
        )
      )
    }

  @Serializable
  sealed interface Config {
    @Serializable
    data object Splash : Config

    @Serializable
    data object Auth : Config

    @Serializable
    data object Home : Config

    @Serializable
    data object Account : Config

    @Serializable
    data object CreateProduction : Config

    @Serializable
    data class ProductionDetails(
      val productionId: String
    ) : Config

    @Serializable
    data class TaskDetails(
      val taskId: String
    ) : Config

    @Serializable
    data class CreateTask(
      val productionId: String,
      val productionTitle: String
    ) : Config

    @Serializable
    data class Chat(
      val taskId: String
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

    data class Account(
      val component: AccountComponent
    ) : Child

    data class CreateProduction(
      val component: CreateProductionComponent
    ) : Child

    data class ProductionDetails(
      val component: ProductionDetailsComponent
    ) : Child

    data class TaskDetails(
      val component: TaskDetailsComponent
    ) : Child

    data class CreateTask(
      val component: CreateTaskComponent
    ) : Child

    data class Chat(
      val component: ChatComponent
    ) : Child
  }
}
