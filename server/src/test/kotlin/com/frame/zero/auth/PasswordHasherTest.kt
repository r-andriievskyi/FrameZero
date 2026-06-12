package com.frame.zero.auth

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {
  private val hasher = PasswordHasher()

  @Test
  fun `verify accepts the original password`() =
    runTest {
      val hash = hasher.hash("hunter2-correct")

      assertTrue(hasher.verify("hunter2-correct", hash))
    }

  @Test
  fun `verify rejects a different password`() =
    runTest {
      val hash = hasher.hash("hunter2-correct")

      assertFalse(hasher.verify("hunter2-wrong", hash))
    }

  @Test
  fun `verify is case sensitive`() =
    runTest {
      val hash = hasher.hash("Hunter2Pass")

      assertFalse(hasher.verify("hunter2pass", hash))
    }

  @Test
  fun `hash produces a different value each call due to salting`() =
    runTest {
      val first = hasher.hash("repeated-input")
      val second = hasher.hash("repeated-input")

      assertNotEquals(first, second)
    }

  @Test
  fun `verify rejects when the stored hash is corrupted`() =
    runTest {
      val hash = hasher.hash("hunter2-correct")
      // Replace the final 5 characters of the hash portion with constants to guarantee a
      // meaningful change — bcrypt's last base64 character has padding bits that are
      // ignored on verify, so a single-char swap can succeed without altering the hash.
      val corrupted = hash.dropLast(5) + "AAAAA"

      assertFalse(hasher.verify("hunter2-correct", corrupted))
    }
}
