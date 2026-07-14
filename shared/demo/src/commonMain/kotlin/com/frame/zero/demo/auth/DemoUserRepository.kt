package com.frame.zero.demo.auth

import com.frame.zero.core.session.UserCache
import com.frame.zero.demo.DemoData
import com.frame.zero.domain.User
import com.frame.zero.repository.user.UserRepository

internal class DemoUserRepository(
  private val userCache: UserCache
) : UserRepository {
  override suspend fun getMe(): User = userCache.load() ?: DemoData.defaultUser
}
