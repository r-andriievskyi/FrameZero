package com.frame.zero.auth

import com.frame.zero.common.testing.H2TestDatabase
import kotlinx.coroutines.runBlocking
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RefreshTokenRepositoryImplTest {
  private val db = H2TestDatabase()
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
      val expiresAt = Instant.now().plusSeconds(3600)

      val record = refreshTokens.create(userId, "hash-1", expiresAt)

      assertEquals(userId, record.userId)
      assertEquals("hash-1", record.tokenHash)
      assertEquals(expiresAt, record.expiresAt)
      assertFalse(record.revoked)
    }

  @Test
  fun `findActiveByHash returns the row when token is active`() =
    runBlocking {
      val userId = users.create("u@x.com", "hash", "", "").id
      refreshTokens.create(userId, "hash-active", Instant.now().plusSeconds(3600))

      val found = refreshTokens.findActiveByHash("hash-active", Instant.now())

      assertNotNull(found)
      assertEquals("hash-active", found.tokenHash)
    }

  @Test
  fun `findActiveByHash returns null when token has been revoked`() =
    runBlocking {
      val userId = users.create("u@x.com", "hash", "", "").id
      refreshTokens.create(userId, "hash-revoked", Instant.now().plusSeconds(3600))
      refreshTokens.revoke("hash-revoked")

      val found = refreshTokens.findActiveByHash("hash-revoked", Instant.now())

      assertNull(found)
    }

  @Test
  fun `findActiveByHash returns null when token has expired`() =
    runBlocking {
      val userId = users.create("u@x.com", "hash", "", "").id
      refreshTokens.create(userId, "hash-expired", Instant.now().minusSeconds(60))

      val found = refreshTokens.findActiveByHash("hash-expired", Instant.now())

      assertNull(found)
    }

  @Test
  fun `findActiveByHash returns null for an unknown hash`() =
    runBlocking {
      assertNull(refreshTokens.findActiveByHash("never-issued", Instant.now()))
    }

  @Test
  fun `revoke flips the flag and returns true`() =
    runBlocking {
      val userId = users.create("u@x.com", "hash", "", "").id
      refreshTokens.create(userId, "hash-1", Instant.now().plusSeconds(3600))

      val revoked = refreshTokens.revoke("hash-1")

      assertTrue(revoked)
      assertNull(refreshTokens.findActiveByHash("hash-1", Instant.now()))
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
      refreshTokens.create(userId, "hash-1", Instant.now().plusSeconds(3600))

      val first = refreshTokens.claim("hash-1", Instant.now())
      val second = refreshTokens.claim("hash-1", Instant.now())

      assertNotNull(first)
      assertTrue(first.revoked)
      assertNull(second, "a second claim of the same token must fail")
    }

  @Test
  fun `claim returns null for an expired token`() =
    runBlocking {
      val userId = users.create("u@x.com", "hash", "", "").id
      refreshTokens.create(userId, "hash-expired", Instant.now().minusSeconds(60))

      assertNull(refreshTokens.claim("hash-expired", Instant.now()))
    }

  @Test
  fun `revokeAllForUser revokes every active token for that user only`() =
    runBlocking {
      val userId = users.create("u@x.com", "hash", "", "").id
      val otherId = users.create("other@x.com", "hash", "", "").id
      refreshTokens.create(userId, "hash-1", Instant.now().plusSeconds(3600))
      refreshTokens.create(userId, "hash-2", Instant.now().plusSeconds(3600))
      refreshTokens.create(otherId, "hash-other", Instant.now().plusSeconds(3600))

      val revokedCount = refreshTokens.revokeAllForUser(userId)

      assertEquals(2, revokedCount)
      assertNotNull(refreshTokens.findActiveByHash("hash-other", Instant.now()))
      assertNull(refreshTokens.findActiveByHash("hash-1", Instant.now()))
      assertNull(refreshTokens.findActiveByHash("hash-2", Instant.now()))
    }
}
