package com.frame.zero.feature.auth.register

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.StateFlow

class RegisterComponent(
  componentContext: ComponentContext,
  val onNavigateToSignIn: () -> Unit,
  viewModelFactory: () -> RegisterViewModel
) : ComponentContext by componentContext {
  private val viewModel: RegisterViewModel = instanceKeeper.getOrCreate { viewModelFactory() }

  val state: StateFlow<RegisterState>
    get() = viewModel.state

  fun onIntent(intent: RegisterIntent) = viewModel.onIntent(intent)
}
