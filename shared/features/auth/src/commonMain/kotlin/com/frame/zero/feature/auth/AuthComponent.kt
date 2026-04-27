package com.frame.zero.feature.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.StateFlow

class AuthComponent(componentContext: ComponentContext, authViewModelFactory: () -> AuthViewModel) :
  ComponentContext by componentContext {
  private val viewModel: AuthViewModel = instanceKeeper.getOrCreate { authViewModelFactory() }

  val state: StateFlow<AuthState>
    get() = viewModel.state

  fun onIntent(intent: AuthIntent) = viewModel.onIntent(intent)
}
