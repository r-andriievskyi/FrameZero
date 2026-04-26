package com.frame.zero.feature.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthComponent(
  componentContext: ComponentContext,
  private val onAuthenticated: () -> Unit,
  viewModel: AuthViewModel,
) : ComponentContext by componentContext {

  // Retain the ViewModel across Android configuration changes via Decompose's InstanceKeeper.
  private val retainedViewModel: AuthViewModel = instanceKeeper.getOrCreate { viewModel }

  val state: StateFlow<AuthState>
    get() = retainedViewModel.state

  init {
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    lifecycle.doOnDestroy { scope.cancel() }
    scope.launch {
      retainedViewModel.events.collect { event ->
        when (event) {
          AuthEvent.Authenticated -> onAuthenticated()
        }
      }
    }
  }

  fun onIntent(intent: AuthIntent) = retainedViewModel.onIntent(intent)
}
