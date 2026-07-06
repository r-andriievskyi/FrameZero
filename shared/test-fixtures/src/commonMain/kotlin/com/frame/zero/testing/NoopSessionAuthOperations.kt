package com.frame.zero.testing

import com.frame.zero.core.session.SessionAuthOperations
import com.frame.zero.domain.User

object NoopSessionAuthOperations : SessionAuthOperations {
  override suspend fun fetchCurrentUser(): User = User(id = "", email = "", firstName = "", lastName = "")

  override suspend fun signOutRemote() = Unit
}
