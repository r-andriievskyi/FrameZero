package com.frame.zero

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.frame.zero.core.navigation.NavigationSignal
import com.frame.zero.core.security.AppLockController
import com.frame.zero.core.session.SessionManager
import com.frame.zero.di.initKoin
import com.frame.zero.feature.RootComponent
import com.frame.zero.feature.account.AccountViewModel
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.auth.register.RegisterViewModel
import com.frame.zero.feature.auth.signin.SignInViewModel
import com.frame.zero.feature.home.HomeComponent
import com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel
import com.frame.zero.feature.home.tab.productions.ProductionsTabViewModel
import com.frame.zero.feature.home.tab.schedule.ScheduleTabViewModel
import com.frame.zero.feature.production.CreateProductionViewModel
import com.frame.zero.feature.production.details.ProductionDetailsViewModel
import com.frame.zero.feature.chat.ChatViewModel
import com.frame.zero.feature.task.create.CreateTaskViewModel
import com.frame.zero.feature.task.details.TaskDetailsViewModel
import com.frame.zero.feature.task.list.TasksListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidEnterBackgroundNotification

private val iosScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

private val iosRoot: RootComponent by lazy {
  val koin = initKoin()
  val sessionManager = koin.get<SessionManager>()
  val appLockController = koin.get<AppLockController>()
  val lifecycle = LifecycleRegistry()
  lifecycle.resume()
  iosScope.launch { sessionManager.initialize() }
  // The iOS process survives backgrounding, so re-lock by observing the system
  // background notification instead of the Decompose lifecycle.
  NSNotificationCenter.defaultCenter.addObserverForName(
    name = UIApplicationDidEnterBackgroundNotification,
    `object` = null,
    queue = NSOperationQueue.mainQueue
  ) { appLockController.onBackgrounded() }
  RootComponent(
    componentContext = DefaultComponentContext(lifecycle = lifecycle),
    sessionManager = sessionManager,
    appLockController = appLockController,
    navigationSignal = koin.get<NavigationSignal>(),
    authComponentFactory = { ctx ->
      AuthComponent(
        componentContext = ctx,
        signInViewModelFactory = { koin.get<SignInViewModel>() },
        registerViewModelFactory = { koin.get<RegisterViewModel>() }
      )
    },
    homeComponentFactory = {
      ctx,
      onCreateProductionClick,
      onProductionClick,
      onAccountClick,
      onTaskClick,
      onTasksClick
      ->
      HomeComponent(
        ctx,
        onAccountClick = onAccountClick,
        onCreateProductionClick = onCreateProductionClick,
        onProductionClick = onProductionClick,
        onTaskClick = onTaskClick,
        onTasksClick = onTasksClick,
        dashboardViewModelFactory = { koin.get<DashboardTabViewModel>() },
        productionsViewModelFactory = { koin.get<ProductionsTabViewModel>() },
        scheduleViewModelFactory = { koin.get<ScheduleTabViewModel>() }
      )
    },
    createProductionViewModelFactory = { koin.get<CreateProductionViewModel>() },
    productionDetailsViewModelFactory = { productionId ->
      koin.get<ProductionDetailsViewModel> { parametersOf(productionId) }
    },
    taskDetailsViewModelFactory = { taskId ->
      koin.get<TaskDetailsViewModel> { parametersOf(taskId) }
    },
    createTaskViewModelFactory = { productionId, productionTitle ->
      koin.get<CreateTaskViewModel> {
        parametersOf(productionId, productionTitle)
      }
    },
    chatViewModelFactory = { taskId ->
      koin.get<ChatViewModel> { parametersOf(taskId) }
    },
    tasksListViewModelFactory = { productionId ->
      koin.get<TasksListViewModel> { parametersOf(productionId) }
    },
    accountViewModelFactory = { koin.get<AccountViewModel>() }
  )
}

@Suppress("ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController { App(iosRoot) }
