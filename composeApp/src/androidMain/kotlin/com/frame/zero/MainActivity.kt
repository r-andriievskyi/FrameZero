package com.frame.zero

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.arkivanov.decompose.defaultComponentContext
import com.frame.zero.core.navigation.DeepLink
import com.frame.zero.core.navigation.NavigationSignal
import com.frame.zero.core.session.SessionManager
import com.frame.zero.feature.RootComponent
import com.frame.zero.push.PushNotifications
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
import com.frame.zero.feature.task.create.CreateTaskViewModel
import com.frame.zero.feature.task.details.TaskDetailsViewModel
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
  private val sessionManager: SessionManager by lazy { application.koin.get() }
  private val navigationSignal: NavigationSignal by lazy { application.koin.get() }

  // False positive on ComponentActivity: the Fragment-version check doesn't apply —
  // registerForActivityResult is natively supported by androidx.activity here.
  @Suppress("InvalidFragmentVersionForActivityResult")
  private val requestNotificationsPermission = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { /* best-effort */ }

  private val root by lazy {
    val koin = application.koin
    RootComponent(
      componentContext = defaultComponentContext(),
      sessionManager = sessionManager,
      navigationSignal = navigationSignal,
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
      createTaskViewModelFactory = { productionId, productionTitle ->
        koin.get<CreateTaskViewModel> {
          parametersOf(productionId, productionTitle)
        }
      },
      accountViewModelFactory = { koin.get<AccountViewModel>() }
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
    )
    super.onCreate(savedInstanceState)
    lifecycleScope.launch { sessionManager.initialize() }
    setContent { App(root) }
    requestNotificationsPermissionIfNeeded()
    handleDeepLinkIntent(intent)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleDeepLinkIntent(intent)
  }

  // todo move
  private fun handleDeepLinkIntent(intent: Intent?) {
    val taskId = intent?.getStringExtra(PushNotifications.EXTRA_TASK_ID) ?: return
    intent.removeExtra(PushNotifications.EXTRA_TASK_ID)
    navigationSignal.emit(DeepLink.TaskDetails(taskId))
  }

  private fun requestNotificationsPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val granted = ContextCompat.checkSelfPermission(
      this, Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
    if (!granted) requestNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
  }
}
