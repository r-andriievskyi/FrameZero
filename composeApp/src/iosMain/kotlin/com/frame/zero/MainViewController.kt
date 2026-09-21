package com.frame.zero

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.frame.zero.di.IosGraphHolder
import com.frame.zero.di.createRootComponent
import com.frame.zero.feature.RootComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidEnterBackgroundNotification

private val iosScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

private val iosRoot: RootComponent by lazy {
  val graph = IosGraphHolder.graph
  val appLockController = graph.appLockController
  val lifecycle = LifecycleRegistry()
  lifecycle.resume()
  iosScope.launch { graph.sessionManager.initialize() }
  // Metro has no eager bindings: resolve the synchronizer so it starts observing the session at
  // launch rather than whenever something first asks for it (Koin: `createdAtStart = true`).
  graph.deviceTokenSynchronizer
  // The iOS process survives backgrounding, so re-lock by observing the system
  // background notification instead of the Decompose lifecycle.
  NSNotificationCenter.defaultCenter.addObserverForName(
    name = UIApplicationDidEnterBackgroundNotification,
    `object` = null,
    queue = NSOperationQueue.mainQueue
  ) { appLockController.onBackgrounded() }
  graph.createRootComponent(DefaultComponentContext(lifecycle = lifecycle))
}

@Suppress("ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController { App(iosRoot) }
