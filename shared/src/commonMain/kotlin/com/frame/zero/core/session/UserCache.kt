package com.frame.zero.core.session

import com.frame.zero.domain.User
import com.russhwolf.settings.Settings

/**
 * Persists the last-known authenticated user so an offline app launch can
 * restore the session without a network round trip. Stored alongside the
 * tokens and cleared with them on logout.
 */
class UserCache(
  private val settings: Settings
) {
  fun save(user: User) {
    settings.putString(KEY_ID, user.id)
    settings.putString(KEY_EMAIL, user.email)
    settings.putString(KEY_FIRST_NAME, user.firstName)
    settings.putString(KEY_LAST_NAME, user.lastName)
  }

  fun load(): User? {
    val id = settings.getStringOrNull(KEY_ID) ?: return null
    val email = settings.getStringOrNull(KEY_EMAIL) ?: return null
    return User(
      id = id,
      email = email,
      firstName = settings.getString(KEY_FIRST_NAME, ""),
      lastName = settings.getString(KEY_LAST_NAME, "")
    )
  }

  fun clear() {
    settings.remove(KEY_ID)
    settings.remove(KEY_EMAIL)
    settings.remove(KEY_FIRST_NAME)
    settings.remove(KEY_LAST_NAME)
  }

  private companion object {
    const val KEY_ID = "auth.user.id"
    const val KEY_EMAIL = "auth.user.email"
    const val KEY_FIRST_NAME = "auth.user.first_name"
    const val KEY_LAST_NAME = "auth.user.last_name"
  }
}
