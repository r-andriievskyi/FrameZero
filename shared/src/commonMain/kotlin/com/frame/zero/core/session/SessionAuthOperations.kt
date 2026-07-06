package com.frame.zero.core.session

import com.frame.zero.domain.User

/**
 * Subset of auth operations the [SessionManager] needs. Implemented by `AuthRepositoryImpl` so the
 * core session layer can stay decoupled from the repositories module.
 */
interface SessionAuthOperations {
  suspend fun fetchCurrentUser(): User

  suspend fun signOutRemote()
}
