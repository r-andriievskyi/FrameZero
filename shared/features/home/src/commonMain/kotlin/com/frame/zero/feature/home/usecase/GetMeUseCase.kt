package com.frame.zero.feature.home.usecase

import com.frame.zero.domain.NoParamsUseCase
import com.frame.zero.domain.User
import com.frame.zero.domain.toDomain
import com.frame.zero.repository.user.UserRepository

class GetMeUseCase(
  private val userRepository: UserRepository
) : NoParamsUseCase<User>() {
  override suspend fun execute(): User = userRepository.getMe().toDomain()
}
