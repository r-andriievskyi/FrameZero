package com.frame.zero.feature.home.usecase

import com.frame.zero.domain.toDomain
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.NoParamsUseCase
import com.frame.zero.domain.User
import com.frame.zero.repository.user.UserRepository
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException

class GetMeUseCase(private val userRepository: UserRepository) : NoParamsUseCase<User>() {
  override fun mapError(throwable: Throwable): DomainError =
    when (throwable) {
      is IOException -> DomainError.Network(throwable.message ?: "Network error")
      is ResponseException -> DomainError.Unknown(throwable.message)
      else -> DomainError.Unknown(throwable.message)
    }

  override suspend fun execute(): User = userRepository.getMe().toDomain()
}
