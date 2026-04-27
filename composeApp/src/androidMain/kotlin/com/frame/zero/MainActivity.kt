package com.frame.zero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.arkivanov.decompose.defaultComponentContext
import com.frame.zero.core.session.SessionManager
import com.frame.zero.feature.RootComponent
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.auth.AuthViewModel
import com.frame.zero.feature.dashboard.DashboardComponent
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private val sessionManager: SessionManager by lazy { application.koin.get() }

  private val root by lazy {
    val koin = application.koin
    RootComponent(
      componentContext = defaultComponentContext(),
      sessionManager = sessionManager,
      authComponentFactory = { ctx -> AuthComponent(ctx) { koin.get<AuthViewModel>() } },
      dashboardComponentFactory = { ctx -> DashboardComponent(ctx) },
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    lifecycleScope.launch { sessionManager.initialize() }
    setContent { App(root) }
  }
}
