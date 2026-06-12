package com.frame.zero.auth

import com.frame.zero.common.testing.PostgresTestDatabase
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RefreshTokenRepositoryImplTest {
  private val db = PostgresTestDatabase()
  private val users = UserRepositoryImpl()
  private val refreshTokens = RefreshTokenRepositoryImpl()

  @BeforeTest
  fun setUp() {
    db.start()
  }

  @AfterTest
  fun tearDown() {
    db.stop()
  }

  @Test
  fun `create persists a refresh token row`() =
    runBlocking {
      val userId = users.create("u@x.com", "hash", "", "").id
      val expiresAt = Clock.System.now() + 3600.seconds

      val record = refreshTokens.create(userId, "hash-1", expiresAt)

      assertEquals(userId, record.userId)
      assertEquals("hash-1", record.tokenHash)
      assertEquals(expiresAt, record.expiresAt)
      assertFalse(record.revoked)
    }

  @Test
  fun `revoke flips the flag and returns true`() =
    runBlocking {
      val userId = users.create("u@x.com", "hash", "", "").id
      refreshTokens.create(userId, "hash-1", Clock.System.now() + 3600.seconds)

      val revoked = refreshTokens.revoke("hash-1")

      assertTrue(revoked)
      assertTrue(refreshTokens.findByHash("hash-1")!!.revoked)
    }

  @Test
  fun `revoke returns false for an unknown hash`() =
    runBlocking {
      assertFalse(refreshTokens.revoke("never-issued"))
    }

  @Test
  fun `claim succeeds exactly once for an active token`() =
    runBlocking {
      val userId = users.create("u@x.com", "hash", "", "").id
      refreshTokens.create(userId, "hash-1", Clock.System.now() + 3600.seconds)

      val first = refreshTokens.claim("hash-1", Clock.System.now())
      val second = refreshTokens.claim("hash-1", Clock.System.now())

      assertNotNull(first)
      assertTrue(first.revoked)
      assertNull(second, "a second claim of the same token must fail")
    }

  @Test
  fun `claim returns null for an expired token`() =
    runBlocking {
      val userId = users.create("u@x.com", "hash", "", "").id
      refreshTokens.create(userId, "hash-expired", Clock.System.now() - 60.seconds)

      assertNull(refreshTokens.claim("hash-expired", Clock.System.now()))
    }

  @Test
  fun `revokeAllForUser revokes every active token for that user only`() =
    runBlocking {
      val userId = users.create("u@x.com", "hash", "", "").id
      val otherId = users.create("other@x.com", "hash", "", "").id
      refreshTokens.create(userId, "hash-1", Clock.System.now() + 3600.seconds)
      refreshTokens.create(userId, "hash-2", Clock.System.now() + 3600.seconds)
      refreshTokens.create(otherId, "hash-other", Clock.System.now() + 3600.seconds)

      val revokedCount = refreshTokens.revokeAllForUser(userId)

      assertEquals(2, revokedCount)
      assertFalse(refreshTokens.findByHash("hash-other")!!.revoked)
      assertTrue(refreshTokens.findByHash("hash-1")!!.revoked)
      assertTrue(refreshTokens.findByHash("hash-2")!!.revoked)
    }
}
