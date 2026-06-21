package com.frame.zero.feature.auth.register

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.Outcome
import com.frame.zero.feature.auth.domain.RegisterUseCase
import com.frame.zero.feature.auth.emptyCredentialsError
import com.frame.zero.feature.auth.isOfflineOrServerError
import com.frame.zero.feature.auth.toUiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class RegisterViewModel(
  private val registerUseCase: RegisterUseCase,
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(RegisterState())
  val state: StateFlow<RegisterState> = _state.asStateFlow()

  fun onIntent(intent: RegisterIntent) {
    when (intent) {
      is RegisterIntent.FirstNameChanged ->
        _state.update { it.copy(firstName = intent.firstName, error = null) }
      is RegisterIntent.LastNameChanged ->
        _state.update { it.copy(lastName = intent.lastName, error = null) }
      is RegisterIntent.EmailChanged ->
        _state.update { it.copy(email = intent.email, error = null) }
      is RegisterIntent.PasswordChanged ->
        _state.update { it.copy(password = intent.password, error = null) }
      RegisterIntent.Submit -> submit()
      RegisterIntent.ToastDismissed -> _state.update { it.copy(errorToast = null) }
    }
  }

  private fun submit() {
    val current = _state.value
    if (current.isLoading) return
    if (current.email.isBlank() || current.password.isBlank()) {
      _state.update { it.copy(error = emptyCredentialsError()) }
      return
    }
    scope.launch {
      _state.update { it.copy(isLoading = true, error = null, errorToast = null) }
      when (
        val outcome =
          registerUseCase(
            RegisterUseCase.Params(
              email = current.email.trim(),
              password = current.password,
              firstName = current.firstName.trim(),
              lastName = current.lastName.trim()
            )
          )
      ) {
        is Outcome.Success -> _state.update { it.copy(isLoading = false) }
        is Outcome.Failure -> {
          val message = outcome.error.toUiText()
          if (outcome.error.isOfflineOrServerError) {
            _state.update { it.copy(isLoading = false, errorToast = message) }
          } else {
            _state.update { it.copy(isLoading = false, error = message) }
          }
        }
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
