package com.frame.zero

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.frame.zero.core.session.SessionManager
import com.frame.zero.di.initKoin
import com.frame.zero.feature.RootComponent
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.auth.AuthViewModel
import com.frame.zero.feature.dashboard.DashboardComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

fun main() {
  val koin = initKoin()
  val sessionManager = koin.get<SessionManager>()
  val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  scope.launch { sessionManager.initialize() }

  val lifecycle = LifecycleRegistry()
  val root =
    RootComponent(
      componentContext = DefaultComponentContext(lifecycle = lifecycle),
      sessionManager = sessionManager,
      authComponentFactory = { ctx -> AuthComponent(ctx) { koin.get<AuthViewModel>() } },
      dashboardComponentFactory = { ctx -> DashboardComponent(ctx) },
    )
  lifecycle.resume()

  application { Window(onCloseRequest = ::exitApplication, title = "FrameZero") { App(root) } }
}
