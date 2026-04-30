package com.frame.zero.feature.auth.signin

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.StateFlow

class SignInComponent(
  componentContext: ComponentContext,
  val onNavigateToRegister: () -> Unit,
  viewModelFactory: () -> SignInViewModel,
) : ComponentContext by componentContext {
  private val viewModel: SignInViewModel = instanceKeeper.getOrCreate { viewModelFactory() }

  val state: StateFlow<SignInState>
    get() = viewModel.state

  fun onIntent(intent: SignInIntent) = viewModel.onIntent(intent)
}
