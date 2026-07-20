package com.frame.zero

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.ReportDrawnWhen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.arkivanov.decompose.defaultComponentContext
import com.frame.zero.core.navigation.NavigationSignal
import com.frame.zero.core.security.AppLockController
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.SessionState
import com.frame.zero.feature.RootComponent
import com.frame.zero.core.files.AndroidFilePicker
import com.frame.zero.push.PushNotificationsRouter
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
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

class MainActivity : FragmentActivity() {
  private val sessionManager: SessionManager by lazy { application.koin.get() }
  private val appLockController: AppLockController by lazy { application.koin.get() }
  private val navigationSignal: NavigationSignal by lazy { application.koin.get() }
  private val pushNotificationsRouter: PushNotificationsRouter by lazy { application.koin.get() }

  // false positive on ComponentActivity: the Fragment-version check doesn't apply —
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
      appLockController = appLockController,
      navigationSignal = navigationSignal,
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

  @OptIn(ExperimentalComposeUiApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen().setKeepOnScreenCondition {
      sessionManager.state.value is SessionState.Loading
    }
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
    )
    super.onCreate(savedInstanceState)
    (application.koin.get<AndroidFilePicker>()).attach(this)
    lifecycleScope.launch { sessionManager.initialize() }
    lifecycleScope.launch {
      appLockController.enabled.collect { enabled ->
        if (enabled) {
          window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
          window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
      }
    }
    setContent {
      val sessionState by sessionManager.state.collectAsStateWithLifecycle()
      ReportDrawnWhen { sessionState !is SessionState.Loading }
      Box(Modifier.semantics { testTagsAsResourceId = true }) { App(root) }
    }
    requestNotificationsPermissionIfNeeded()
    pushNotificationsRouter.route(intent)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    pushNotificationsRouter.route(intent)
  }

  private fun requestNotificationsPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val granted = ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
    if (!granted) requestNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
  }
}
