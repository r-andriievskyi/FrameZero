package com.frame.zero

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.frame.zero.di.initKoin
import com.frame.zero.feature.RootComponent
import com.frame.zero.feature.auth.AuthViewModel

private val iosRoot: RootComponent by lazy {
  val koin = initKoin()
  val lifecycle = LifecycleRegistry()
  lifecycle.resume()
  RootComponent(
    componentContext = DefaultComponentContext(lifecycle = lifecycle),
    authViewModelFactory = { koin.get<AuthViewModel>() },
  )
}

fun MainViewController() = ComposeUIViewController { App(iosRoot) }
