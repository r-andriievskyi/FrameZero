package com.frame.zero.feature.auth.domain

import com.frame.zero.core.session.SessionManager
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.UseCase
import com.frame.zero.domain.User
import com.frame.zero.repository.auth.AuthRepository

class RegisterUseCase(
  private val authRepository: AuthRepository,
  private val sessionManager: SessionManager
) : UseCase<RegisterUseCase.Params, User>() {
  data class Params(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
  )

  override fun mapError(throwable: Throwable): DomainError = throwable.toDomainError()

  override suspend fun execute(params: Params): User {
    val user =
      authRepository
        .register(params.email, params.password, params.firstName, params.lastName)
    sessionManager.onAuthenticated(user)
    return user
  }
}
