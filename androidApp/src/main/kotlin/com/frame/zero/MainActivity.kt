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
import com.frame.zero.core.session.SessionState
import com.frame.zero.di.createRootComponent
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
  private val graph by lazy { application.graph }
  private val sessionManager by lazy { graph.sessionManager }
  private val appLockController by lazy { graph.appLockController }
  private val pushNotificationsRouter by lazy { graph.pushNotificationsRouter }

  // false positive on ComponentActivity: the Fragment-version check doesn't apply —
  // registerForActivityResult is natively supported by androidx.activity here.
  @Suppress("InvalidFragmentVersionForActivityResult")
  private val requestNotificationsPermission = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { /* best-effort */ }

  private val root by lazy { graph.createRootComponent(defaultComponentContext()) }

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
    graph.filePicker.attach(this)
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
