package com.frame.zero.feature.auth.signin

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.Outcome
import com.frame.zero.feature.auth.EMPTY_CREDENTIALS_MESSAGE
import com.frame.zero.feature.auth.domain.LoginUseCase
import com.frame.zero.feature.auth.toUserMessage
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

class SignInViewModel(
  private val loginUseCase: LoginUseCase,
  dispatcher: CoroutineContext = Dispatchers.Main,
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(SignInState())
  val state: StateFlow<SignInState> = _state.asStateFlow()

  fun onIntent(intent: SignInIntent) {
    when (intent) {
      is SignInIntent.EmailChanged -> _state.update { it.copy(email = intent.email, error = null) }
      is SignInIntent.PasswordChanged ->
        _state.update { it.copy(password = intent.password, error = null) }
      SignInIntent.Submit -> submit()
    }
  }

  private fun submit() {
    val current = _state.value
    if (current.isLoading) return
    if (current.email.isBlank() || current.password.isBlank()) {
      _state.update { it.copy(error = EMPTY_CREDENTIALS_MESSAGE) }
      return
    }
    scope.launch {
      _state.update { it.copy(isLoading = true, error = null) }
      when (val outcome =
        loginUseCase(LoginUseCase.Params(current.email.trim(), current.password))) {
        is Outcome.Success -> _state.update { it.copy(isLoading = false) }
        is Outcome.Failure ->
          _state.update { it.copy(isLoading = false, error = outcome.error.toUserMessage()) }
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
