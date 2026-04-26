package com.frame.zero

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.frame.zero.feature.RootComponent
import com.frame.zero.feature.auth.AuthViewModel
import com.frame.zero.feature.initKoin

fun main() {
  val koin = initKoin()
  val lifecycle = LifecycleRegistry()
  val root =
    RootComponent(
      componentContext = DefaultComponentContext(lifecycle = lifecycle),
      authViewModelFactory = { koin.get<AuthViewModel>() },
    )
  lifecycle.resume()
  application { Window(onCloseRequest = ::exitApplication, title = "FrameZero") { App(root) } }
}
