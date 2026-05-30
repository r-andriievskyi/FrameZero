package com.frame.zero

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
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
import com.frame.zero.feature.task.details.TaskDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

private val iosScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

private val iosRoot: RootComponent by lazy {
  val koin = initKoin()
  val sessionManager = koin.get<SessionManager>()
  val lifecycle = LifecycleRegistry()
  lifecycle.resume()
  iosScope.launch { sessionManager.initialize() }
  RootComponent(
    componentContext = DefaultComponentContext(lifecycle = lifecycle),
    sessionManager = sessionManager,
    authComponentFactory = { ctx ->
      AuthComponent(
        componentContext = ctx,
        signInViewModelFactory = { koin.get<SignInViewModel>() },
        registerViewModelFactory = { koin.get<RegisterViewModel>() }
      )
    },
    homeComponentFactory = { ctx, onCreateProductionClick, onProductionClick, onAccountClick, onTaskClick ->
      HomeComponent(
        ctx,
        onAccountClick = onAccountClick,
        onCreateProductionClick = onCreateProductionClick,
        onProductionClick = onProductionClick,
        onTaskClick = onTaskClick,
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
    accountViewModelFactory = { koin.get<AccountViewModel>() }
  )
}

@Suppress("ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController { App(iosRoot) }
