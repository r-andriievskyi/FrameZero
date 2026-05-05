package com.frame.zero.auth

import com.frame.zero.auth.AuthError.EmailAlreadyExists
import com.frame.zero.auth.AuthError.InvalidCredentials
import com.frame.zero.auth.AuthError.InvalidInput
import com.frame.zero.auth.AuthError.InvalidRefreshToken
import com.frame.zero.auth.dto.AuthResponse
import com.frame.zero.auth.dto.RefreshResponse
import com.frame.zero.auth.dto.UserDto
import com.frame.zero.config.JwtConfig
import com.frame.zero.repository.RefreshTokenRepository
import com.frame.zero.repository.UserRecord
import com.frame.zero.repository.UserRepository
import java.time.Instant
import java.util.UUID

class AuthService(
  private val users: UserRepository,
  private val refreshTokens: RefreshTokenRepository,
  private val passwordHasher: PasswordHasher,
  private val tokenHasher: TokenHasher,
  private val jwtService: JwtService,
  private val jwtConfig: JwtConfig,
) {
  suspend fun register(
    email: String,
    password: String,
    firstName: String,
    lastName: String,
  ): AuthResponse {
    validateEmail(email)
    validatePassword(password)
    val normalized = email.trim().lowercase()
    if (users.findByEmail(normalized) != null) throw AuthException(EmailAlreadyExists)
    val user =
      users.create(
        email = normalized,
        passwordHash = passwordHasher.hash(password),
        firstName = firstName.trim(),
        lastName = lastName.trim(),
      )
    return issueTokens(user)
  }

  suspend fun login(
    email: String,
    password: String
  ): AuthResponse {
    val normalized = email.trim().lowercase()
    val user = users.findByEmail(normalized) ?: throw AuthException(InvalidCredentials)
    if (!passwordHasher.verify(password, user.passwordHash)) throw AuthException(InvalidCredentials)
    return issueTokens(user)
  }

  suspend fun refresh(refreshToken: String): RefreshResponse {
    val hash = tokenHasher.sha256(refreshToken)
    val now = Instant.now()
    val record =
      refreshTokens.findActiveByHash(hash, now) ?: throw AuthException(InvalidRefreshToken)
    refreshTokens.revoke(hash)
    val user = users.findById(record.userId) ?: throw AuthException(InvalidRefreshToken)
    val (newRefresh, newRefreshHash, expiresAt) = newRefreshToken()
    refreshTokens.create(userId = user.id, tokenHash = newRefreshHash, expiresAt = expiresAt)
    return RefreshResponse(
      accessToken = jwtService.createAccessToken(user.id, user.email),
      refreshToken = newRefresh,
    )
  }

  suspend fun logout(refreshToken: String) {
    refreshTokens.revoke(tokenHasher.sha256(refreshToken))
  }

  suspend fun me(userId: UUID): UserDto? = users.findById(userId)?.toDto()

  private suspend fun issueTokens(user: UserRecord): AuthResponse {
    val (refresh, refreshHash, expiresAt) = newRefreshToken()
    refreshTokens.create(userId = user.id, tokenHash = refreshHash, expiresAt = expiresAt)
    return AuthResponse(
      accessToken = jwtService.createAccessToken(user.id, user.email),
      refreshToken = refresh,
      user = user.toDto(),
    )
  }

  private fun newRefreshToken(): Triple<String, String, Instant> {
    val token = tokenHasher.generateOpaqueToken()
    val hash = tokenHasher.sha256(token)
    val expiresAt = Instant.now().plusMillis(jwtConfig.refreshTokenTtl.inWholeMilliseconds)
    return Triple(token, hash, expiresAt)
  }

  private fun UserRecord.toDto(): UserDto =
    UserDto(id = id.toString(), email = email, firstName = firstName, lastName = lastName)

  private fun validateEmail(email: String) {
    if (!EMAIL_REGEX.matches(email.trim())) {
      throw AuthException(InvalidInput("Invalid email format"))
    }
  }

  private fun validatePassword(password: String) {
    if (password.length < MIN_PASSWORD_LENGTH) {
      throw AuthException(InvalidInput("Password must be at least $MIN_PASSWORD_LENGTH characters"))
    }
  }

  private companion object {
    const val MIN_PASSWORD_LENGTH = 8
    val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
  }
}
