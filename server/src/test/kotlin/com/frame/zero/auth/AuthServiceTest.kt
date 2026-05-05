package com.frame.zero.auth

import com.frame.zero.auth.testing.FakeRefreshTokenRepository
import com.frame.zero.auth.testing.FakeUserRepository
import com.frame.zero.config.JwtConfig
import com.frame.zero.repository.RefreshTokenRepository
import com.frame.zero.repository.UserRepository
import kotlinx.coroutines.test.runTest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class AuthServiceTest {
  private val baseJwtConfig =
    JwtConfig(
      secret = "test-secret-must-be-long-enough-for-hmac256",
      issuer = "test-issuer",
      audience = "test-audience",
      realm = "test-realm",
      accessTokenTtl = 15.minutes,
      refreshTokenTtl = 30.days
    )

  // -- register --------------------------------------------------------------

  @Test
  fun `register rejects empty email with InvalidInput`() =
    runTest {
      val service = makeService()

      val ex = assertFailsWith<AuthException> { service.register("", "password123", "", "") }

      assertEquals(AuthError.InvalidInput("Invalid email format"), ex.error)
    }

  @Test
  fun `register rejects malformed email`() =
    runTest {
      val service = makeService()

      val ex =
        assertFailsWith<AuthException> { service.register("not-an-email", "password123", "", "") }

      assertEquals(AuthError.InvalidInput("Invalid email format"), ex.error)
    }

  @Test
  fun `register rejects password shorter than the minimum length`() =
    runTest {
      val service = makeService()

      val ex = assertFailsWith<AuthException> { service.register("u@x.com", "short", "", "") }

      val invalid = assertIs<AuthError.InvalidInput>(ex.error)
      assertEquals("Password must be at least 8 characters", invalid.reason)
    }

  @Test
  fun `register normalizes email to lowercase and trims whitespace`() =
    runTest {
      val users = FakeUserRepository()
      val service = makeService(users = users)

      service.register("  Foo@EXAMPLE.com  ", "password123", "", "")

      assertNotNull(users.findByEmail("foo@example.com"))
    }

  @Test
  fun `register rejects duplicate email regardless of casing`() =
    runTest {
      val users = FakeUserRepository()
      val service = makeService(users = users)
      service.register("u@x.com", "password123", "", "")

      val ex = assertFailsWith<AuthException> { service.register("U@X.com", "password456", "", "") }

      assertEquals(AuthError.EmailAlreadyExists, ex.error)
    }

  @Test
  fun `register persists hashed password and creates a refresh token row`() =
    runTest {
      val users = FakeUserRepository()
      val tokens = FakeRefreshTokenRepository()
      val passwordHasher = PasswordHasher()
      val service = makeService(users = users, tokens = tokens, passwordHasher = passwordHasher)

      val response = service.register("u@x.com", "password123", "", "")

      val record = users.findByEmail("u@x.com")
      assertNotNull(record)
      assertNotEquals("password123", record.passwordHash)
      assertTrue(passwordHasher.verify("password123", record.passwordHash))
      assertEquals(1, tokens.records.size)
      assertEquals(record.id, tokens.records.first().userId)
      assertEquals("u@x.com", response.user.email)
    }

  @Test
  fun `register stores refresh token hashed not in plaintext`() =
    runTest {
      val tokens = FakeRefreshTokenRepository()
      val tokenHasher = TokenHasher()
      val service = makeService(tokens = tokens, tokenHasher = tokenHasher)

      val response = service.register("u@x.com", "password123", "", "")

      val stored = tokens.records.single()
      assertEquals(tokenHasher.sha256(response.refreshToken), stored.tokenHash)
      assertNotEquals(response.refreshToken, stored.tokenHash)
    }

  // -- login -----------------------------------------------------------------

  @Test
  fun `login with unknown email returns InvalidCredentials`() =
    runTest {
      val service = makeService()

      val ex = assertFailsWith<AuthException> { service.login("nobody@x.com", "password123") }

      assertEquals(AuthError.InvalidCredentials, ex.error)
    }

  @Test
  fun `login with wrong password returns InvalidCredentials`() =
    runTest {
      val service = makeService()
      service.register("u@x.com", "correct-password", "", "")

      val ex = assertFailsWith<AuthException> { service.login("u@x.com", "wrong-password") }

      assertEquals(AuthError.InvalidCredentials, ex.error)
    }

  @Test
  fun `login with correct credentials returns auth response and creates a refresh row`() =
    runTest {
      val tokens = FakeRefreshTokenRepository()
      val service = makeService(tokens = tokens)
      service.register("u@x.com", "password123", "", "")
      val tokenCountAfterRegister = tokens.records.size

      val response = service.login("u@x.com", "password123")

      assertEquals("u@x.com", response.user.email)
      assertEquals(tokenCountAfterRegister + 1, tokens.records.size)
    }

  @Test
  fun `login normalizes email casing and whitespace`() =
    runTest {
      val service = makeService()
      service.register("u@x.com", "password123", "", "")

      val response = service.login("  U@X.com  ", "password123")

      assertEquals("u@x.com", response.user.email)
    }

  // -- refresh ---------------------------------------------------------------

  @Test
  fun `refresh with unknown token returns InvalidRefreshToken`() =
    runTest {
      val service = makeService()

      val ex = assertFailsWith<AuthException> { service.refresh("never-issued") }

      assertEquals(AuthError.InvalidRefreshToken, ex.error)
    }

  @Test
  fun `refresh with revoked token returns InvalidRefreshToken`() =
    runTest {
      val service = makeService()
      val auth = service.register("u@x.com", "password123", "", "")
      service.logout(auth.refreshToken)

      val ex = assertFailsWith<AuthException> { service.refresh(auth.refreshToken) }

      assertEquals(AuthError.InvalidRefreshToken, ex.error)
    }

  @Test
  fun `refresh with expired token returns InvalidRefreshToken`() =
    runTest {
      val service = makeService(jwtConfig = baseJwtConfig.copy(refreshTokenTtl = (-1).milliseconds))
      val auth = service.register("u@x.com", "password123", "", "")

      val ex = assertFailsWith<AuthException> { service.refresh(auth.refreshToken) }

      assertEquals(AuthError.InvalidRefreshToken, ex.error)
    }

  @Test
  fun `refresh with valid token revokes the old one and returns a new pair`() =
    runTest {
      val tokens = FakeRefreshTokenRepository()
      val tokenHasher = TokenHasher()
      val service = makeService(tokens = tokens, tokenHasher = tokenHasher)
      val auth = service.register("u@x.com", "password123", "", "")
      val originalHash = tokenHasher.sha256(auth.refreshToken)

      val response = service.refresh(auth.refreshToken)

      assertNotEquals(auth.refreshToken, response.refreshToken)
      val original = tokens.records.first { it.tokenHash == originalHash }
      assertTrue(original.revoked)
      assertEquals(2, tokens.records.size)
    }

  @Test
  fun `refresh fails when the user no longer exists`() =
    runTest {
      val users = FakeUserRepository()
      val service = makeService(users = users)
      val auth = service.register("u@x.com", "password123", "", "")
      users.deleteAll()

      val ex = assertFailsWith<AuthException> { service.refresh(auth.refreshToken) }

      assertEquals(AuthError.InvalidRefreshToken, ex.error)
    }

  // -- logout ----------------------------------------------------------------

  @Test
  fun `logout revokes the refresh token`() =
    runTest {
      val tokens = FakeRefreshTokenRepository()
      val tokenHasher = TokenHasher()
      val service = makeService(tokens = tokens, tokenHasher = tokenHasher)
      val auth = service.register("u@x.com", "password123", "", "")

      service.logout(auth.refreshToken)

      val record = tokens.records.first { it.tokenHash == tokenHasher.sha256(auth.refreshToken) }
      assertTrue(record.revoked)
    }

  @Test
  fun `logout with unknown token does not throw`() =
    runTest {
      val service = makeService()

      service.logout("not-a-real-token")
      // No exception is the assertion.
    }

  // -- me --------------------------------------------------------------------

  @Test
  fun `me returns UserDto for a known user id`() =
    runTest {
      val service = makeService()
      val auth = service.register("u@x.com", "password123", "", "")

      val dto = service.me(UUID.fromString(auth.user.id))

      assertEquals("u@x.com", dto?.email)
      assertEquals(auth.user.id, dto?.id)
    }

  @Test
  fun `me returns null for an unknown user id`() =
    runTest {
      val service = makeService()

      val dto = service.me(UUID.randomUUID())

      assertNull(dto)
    }

  // -- helpers ---------------------------------------------------------------

  private fun makeService(
    users: UserRepository = FakeUserRepository(),
    tokens: RefreshTokenRepository = FakeRefreshTokenRepository(),
    passwordHasher: PasswordHasher = PasswordHasher(),
    tokenHasher: TokenHasher = TokenHasher(),
    jwtConfig: JwtConfig = baseJwtConfig
  ): AuthService =
    AuthService(
      users = users,
      refreshTokens = tokens,
      passwordHasher = passwordHasher,
      tokenHasher = tokenHasher,
      jwtService = JwtService(jwtConfig),
      jwtConfig = jwtConfig
    )
}
