package com.frame.zero.auth

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.auth.dto.AuthResponse
import com.frame.zero.auth.dto.RefreshResponse
import com.frame.zero.auth.dto.UserDto
import com.frame.zero.common.Transactor
import com.frame.zero.common.Validators
import com.frame.zero.common.nowTruncatedToMicros
import com.frame.zero.config.JwtConfig
import java.util.UUID
import kotlin.time.Instant

class AuthService(
  private val users: UserRepository,
  private val refreshTokens: RefreshTokenRepository,
  private val passwordHasher: PasswordHasher,
  private val tokenHasher: TokenHasher,
  private val jwtService: JwtService,
  private val jwtConfig: JwtConfig,
  private val transactor: Transactor
) {
  suspend fun register(
    email: String,
    password: String,
    firstName: String,
    lastName: String
  ): AuthResponse {
    validateRegistration(email, password, firstName, lastName)
    val normalized = email.trim().lowercase()
    // Hash before opening the transaction so bcrypt never holds a DB connection.
    val passwordHash = passwordHasher.hash(password)
    return transactor.transaction {
      if (users.findByEmail(normalized) != null) throw AppException(AppError.EmailAlreadyExists)
      val user = users.create(
        email = normalized,
        passwordHash = passwordHash,
        firstName = firstName.trim(),
        lastName = lastName.trim()
      )
      issueTokens(user)
    }
  }

  suspend fun login(
    email: String,
    password: String
  ): AuthResponse {
    val normalized = email.trim().lowercase()
    val user =
      transactor.transaction { users.findByEmail(normalized) } ?: throw AppException(AppError.InvalidCredentials)
    // Verify outside the transaction — bcrypt is CPU-bound, not DB work.
    if (!passwordHasher.verify(password, user.passwordHash)) throw AppException(AppError.InvalidCredentials)
    return transactor.transaction { issueTokens(user) }
  }

  suspend fun refresh(refreshToken: String): RefreshResponse {
    val hash = tokenHasher.sha256(refreshToken)
    val now = nowTruncatedToMicros()

    // Claim + rotate atomically. A successful claim revokes the old token and
    // issues a new pair in the same transaction.
    val rotated = transactor.transaction {
      val record = refreshTokens.claim(hash, now) ?: return@transaction null
      val user = users.findById(record.userId) ?: return@transaction null
      val (newRefresh, newRefreshHash, expiresAt) = newRefreshToken()
      refreshTokens.create(userId = user.id, tokenHash = newRefreshHash, expiresAt = expiresAt)
      RefreshResponse(
        accessToken = jwtService.createAccessToken(user.id, user.email),
        refreshToken = newRefresh
      )
    }
    if (rotated != null) return rotated

    // Claim failed: a refresh attempt with an already-revoked token means it was
    // rotated before — either replayed by a racing client or stolen. Treat it as
    // theft and kill every session for that user, in its own committed
    // transaction so the revocation survives the rejection below.
    transactor.transaction {
      val known = refreshTokens.findByHash(hash)
      if (known != null && known.revoked) refreshTokens.revokeAllForUser(known.userId)
    }
    throw AppException(AppError.InvalidRefreshToken)
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
      user = user.toDto()
    )
  }

  private fun newRefreshToken(): Triple<String, String, Instant> {
    val token = tokenHasher.generateOpaqueToken()
    val hash = tokenHasher.sha256(token)
    val expiresAt = nowTruncatedToMicros() + jwtConfig.refreshTokenTtl
    return Triple(token, hash, expiresAt)
  }

  private fun UserRecord.toDto(): UserDto =
    UserDto(id = id.toString(), email = email, firstName = firstName, lastName = lastName)

  private fun validateRegistration(
    email: String,
    password: String,
    firstName: String,
    lastName: String
  ) {
    val errors = mutableMapOf<String, String>()
    val trimmedEmail = email.trim()
    when {
      trimmedEmail.length > MAX_EMAIL_LENGTH ->
        errors["email"] = "Email must be at most $MAX_EMAIL_LENGTH characters"
      !Validators.isValidEmail(trimmedEmail) -> errors["email"] = "Invalid email format"
    }
    if (password.length < MIN_PASSWORD_LENGTH) {
      errors["password"] = "Password must be at least $MIN_PASSWORD_LENGTH characters"
    }
    validateName(firstName, "firstName", errors)
    validateName(lastName, "lastName", errors)
    if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))
  }

  private fun validateName(
    name: String,
    field: String,
    errors: MutableMap<String, String>
  ) {
    when {
      name.isBlank() -> errors[field] = "$field is required"
      name.trim().length > MAX_NAME_LENGTH -> errors[field] = "$field must be at most $MAX_NAME_LENGTH characters"
    }
  }

  private companion object {
    const val MIN_PASSWORD_LENGTH = 8

    // match the column sizes in UsersTable.
    const val MAX_EMAIL_LENGTH = 320
    const val MAX_NAME_LENGTH = 100
  }
}
