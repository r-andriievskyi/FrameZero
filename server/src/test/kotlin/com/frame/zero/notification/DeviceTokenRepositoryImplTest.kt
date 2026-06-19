package com.frame.zero.notification

import com.frame.zero.auth.UserRepositoryImpl
import com.frame.zero.common.testing.PostgresTestDatabase
import com.frame.zero.dto.device.DevicePlatform
import kotlinx.coroutines.runBlocking
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeviceTokenRepositoryImplTest {
  private val db = PostgresTestDatabase()
  private val users = UserRepositoryImpl()
  private val deviceTokens = DeviceTokenRepositoryImpl()

  @BeforeTest
  fun setUp() {
    db.start()
  }

  @AfterTest
  fun tearDown() {
    db.stop()
  }

  private suspend fun newUser(email: String): UUID = users.create(email, "h", "Us", "Er").id

  @Test
  fun `upsert registers a token that findTokensForUser returns`() =
    runBlocking {
      val userId = newUser("u@x.com")

      deviceTokens.upsert(userId, "token-1", DevicePlatform.ANDROID)

      assertEquals(listOf("token-1"), deviceTokens.findTokensForUser(userId))
    }

  @Test
  fun `re-registering the same token moves it to the new owner`() =
    runBlocking {
      val first = newUser("first@x.com")
      val second = newUser("second@x.com")
      deviceTokens.upsert(first, "shared-token", DevicePlatform.ANDROID)

      deviceTokens.upsert(second, "shared-token", DevicePlatform.IOS)

      assertTrue(deviceTokens.findTokensForUser(first).isEmpty(), "token moved away from the first user")
      assertEquals(listOf("shared-token"), deviceTokens.findTokensForUser(second))
    }

  @Test
  fun `a user can have multiple device tokens`() =
    runBlocking {
      val userId = newUser("u@x.com")
      deviceTokens.upsert(userId, "phone", DevicePlatform.ANDROID)
      deviceTokens.upsert(userId, "tablet", DevicePlatform.ANDROID)

      assertEquals(setOf("phone", "tablet"), deviceTokens.findTokensForUser(userId).toSet())
    }

  @Test
  fun `delete removes the token`() =
    runBlocking {
      val userId = newUser("u@x.com")
      deviceTokens.upsert(userId, "token-1", DevicePlatform.ANDROID)

      deviceTokens.delete("token-1")

      assertTrue(deviceTokens.findTokensForUser(userId).isEmpty())
    }
}
