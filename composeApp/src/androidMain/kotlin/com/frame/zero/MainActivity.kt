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
import com.frame.zero.feature.auth.register.RegisterViewModel
import com.frame.zero.feature.auth.signin.SignInViewModel
import com.frame.zero.feature.home.HomeComponent
import com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel
import com.frame.zero.feature.home.tab.projects.ProjectsTabViewModel
import com.frame.zero.feature.home.tab.schedule.ScheduleTabViewModel
import com.frame.zero.feature.production.CreateProductionViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private val sessionManager: SessionManager by lazy { application.koin.get() }

  private val root by lazy {
    val koin = application.koin
    RootComponent(
      componentContext = defaultComponentContext(),
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

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    lifecycleScope.launch { sessionManager.initialize() }
    setContent { App(root) }
  }
}
