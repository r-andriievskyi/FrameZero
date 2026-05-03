package com.frame.zero.core.session

import com.frame.zero.auth.dto.UserDto

/**
 * Subset of auth operations the [SessionManager] needs. Implemented by `AuthRepositoryImpl` so the
 * core session layer can stay decoupled from the repositories module.
 */
interface SessionAuthOperations {
  suspend fun fetchCurrentUser(): UserDto

  suspend fun signOutRemote()
}
