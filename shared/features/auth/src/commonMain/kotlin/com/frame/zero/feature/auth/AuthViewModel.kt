package com.frame.zero.feature.auth

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.feature.auth.usecase.LoginUseCase
import com.frame.zero.feature.auth.usecase.RegisterUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
  private val loginUseCase: LoginUseCase,
  private val registerUseCase: RegisterUseCase,
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  private val _state = MutableStateFlow(AuthState())
  val state: StateFlow<AuthState> = _state.asStateFlow()

  private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
  val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

  fun onIntent(intent: AuthIntent) {
    when (intent) {
      is AuthIntent.EmailChanged -> _state.update { it.copy(email = intent.value) }
      is AuthIntent.PasswordChanged -> _state.update { it.copy(password = intent.value) }
      AuthIntent.LoginClicked -> submit(login = true)
      AuthIntent.RegisterClicked -> submit(login = false)
    }
  }

  private fun submit(login: Boolean) {
    val email = _state.value.email
    val password = _state.value.password
    if (email.isBlank() || password.isBlank()) {
      _state.update { it.copy(error = "Email and password must not be empty") }
      return
    }
    scope.launch {
      _state.update { it.copy(isLoading = true, error = null) }
      val outcome = if (login) loginUseCase(email, password) else registerUseCase(email, password)
      when (outcome) {
        is Outcome.Success -> {
          _state.update { it.copy(isLoading = false) }
          _events.emit(AuthEvent.Authenticated)
        }
        is Outcome.Failure -> {
          _state.update { it.copy(isLoading = false, error = outcome.error.toMessage()) }
        }
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
