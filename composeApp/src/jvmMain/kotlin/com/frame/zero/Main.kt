package com.frame.zero

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
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
import kotlinx.coroutines.launch

fun main() {
  val lifecycle = LifecycleRegistry()
  lifecycle.resume()

  val koin = initKoin()

  val root = RootComponent(
    componentContext = DefaultComponentContext(lifecycle),
    sessionManager = koin.get(),
    authComponentFactory = { ctx ->
      AuthComponent(
        componentContext = ctx,
        signInViewModelFactory = { koin.get<SignInViewModel>() },
        registerViewModelFactory = { koin.get<RegisterViewModel>() }
      )
    },
    homeComponentFactory = { ctx, onCreateProductionClick ->
      HomeComponent(
        componentContext = ctx,
        onCreateProductionClick = onCreateProductionClick,
        dashboardViewModelFactory = { koin.get<DashboardTabViewModel>() },
        projectsViewModelFactory = { koin.get<ProjectsTabViewModel>() },
        scheduleViewModelFactory = { koin.get<ScheduleTabViewModel>() }
      )
    },
    createProductionViewModelFactory = { koin.get<CreateProductionViewModel>() }
  )

  CoroutineScope(Dispatchers.IO).launch {
    koin.get<com.frame.zero.core.session.SessionManager>().initialize()
  }

  application {
    Window(
      onCloseRequest = ::exitApplication,
      title = "FrameZero"
    ) {
      App(root)
    }
  }
}
