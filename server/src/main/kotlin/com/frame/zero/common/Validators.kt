package com.frame.zero.common

/** Shared request-validation primitives so the rules live in one place. */
object Validators {
  // Pragmatic email shape check — not RFC-complete, but rejects the obvious
  // garbage before anything touches the database.
  private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

  fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)
}
