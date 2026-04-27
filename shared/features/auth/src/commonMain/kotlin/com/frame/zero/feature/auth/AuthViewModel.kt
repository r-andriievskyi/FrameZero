package com.frame.zero.feature.auth

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.feature.auth.usecase.LoginUseCase
import com.frame.zero.feature.auth.usecase.RegisterUseCase
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
  private val loginUseCase: LoginUseCase,
  private val registerUseCase: RegisterUseCase,
  dispatcher: CoroutineContext = Dispatchers.Main,
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(AuthState())
  val state: StateFlow<AuthState> = _state.asStateFlow()

  fun onIntent(intent: AuthIntent) {
    when (intent) {
      is AuthIntent.Login -> submit(intent.email, intent.password, register = false)
      is AuthIntent.Register -> submit(intent.email, intent.password, register = true)
      AuthIntent.SwitchMode ->
        _state.update {
          val next = if (it.mode == AuthMode.Login) AuthMode.Register else AuthMode.Login
          it.copy(mode = next, error = null)
        }
    }
  }

  private fun submit(email: String, password: String, register: Boolean) {
    if (email.isBlank() || password.isBlank()) {
      _state.update { it.copy(error = "Email and password must not be empty") }
      return
    }
    if (_state.value.isLoading) return
    scope.launch {
      _state.update { it.copy(isLoading = true, error = null) }
      val outcome =
        if (register) registerUseCase(email, password) else loginUseCase(email, password)
      when (outcome) {
        is Outcome.Success -> _state.update { it.copy(isLoading = false) }
        is Outcome.Failure ->
          _state.update { it.copy(isLoading = false, error = outcome.error.toMessage()) }
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }

  private fun DomainError.toMessage(): String =
    when (this) {
      DomainError.InvalidCredentials -> "Invalid email or password"
      DomainError.EmailAlreadyExists -> "An account with this email already exists"
      is DomainError.Network -> "Network error: $message"
      is DomainError.Unknown -> message ?: "Something went wrong"
    }
}
