package com.frame.zero

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.frame.zero.core.session.SessionManager
import com.frame.zero.di.initKoin
import com.frame.zero.feature.RootComponent
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.auth.register.RegisterViewModel
import com.frame.zero.feature.auth.signin.SignInViewModel
import com.frame.zero.feature.home.HomeComponent
import com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel
import com.frame.zero.feature.home.tab.projects.ProjectsTabViewModel
import com.frame.zero.feature.home.tab.schedule.ScheduleTabViewModel
import com.frame.zero.feature.production.CreateProductionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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
    homeComponentFactory = { ctx, onCreateProductionClick ->
      HomeComponent(
        ctx,
        onCreateProductionClick = onCreateProductionClick,
        dashboardViewModelFactory = { koin.get<DashboardTabViewModel>() },
        projectsViewModelFactory = { koin.get<ProjectsTabViewModel>() },
        scheduleViewModelFactory = { koin.get<ScheduleTabViewModel>() }
      )
    },
    createProductionViewModelFactory = { koin.get<CreateProductionViewModel>() }
  )
}

@Suppress("ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController { App(iosRoot) }
