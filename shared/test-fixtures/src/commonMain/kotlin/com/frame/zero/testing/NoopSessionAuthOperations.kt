package com.frame.zero.testing

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.core.session.SessionAuthOperations

object NoopSessionAuthOperations : SessionAuthOperations {
  override suspend fun fetchCurrentUser(): UserDto = UserDto(id = "", email = "", firstName = "", lastName = "")

  override suspend fun signOutRemote() = Unit
}
