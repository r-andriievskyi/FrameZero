package com.frame.zero.feature.auth.usecase

import com.frame.zero.core.session.SessionManager
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.User
import com.frame.zero.domain.onSuccess
import com.frame.zero.repository.auth.AuthRepository

class LoginUseCase(
  private val authRepository: AuthRepository,
  private val sessionManager: SessionManager,
) {
  suspend operator fun invoke(email: String, password: String): Outcome<User> =
    authRepository.login(email, password).onSuccess(sessionManager::onAuthenticated)
}
