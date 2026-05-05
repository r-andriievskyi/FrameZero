package com.frame.zero.repository

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

class RefreshTokenRepositoryExposedTest {
  private val db = H2TestDatabase()
  private val users = UserRepositoryExposed()
  private val refreshTokens = RefreshTokenRepositoryExposed()

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
}
