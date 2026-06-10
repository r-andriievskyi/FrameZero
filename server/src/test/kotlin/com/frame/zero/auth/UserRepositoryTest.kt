package com.frame.zero.auth

import com.frame.zero.common.testing.H2TestDatabase
import kotlinx.coroutines.runBlocking
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserRepositoryTest {
  private val db = H2TestDatabase()
  private val repository = UserRepositoryImpl()

  @BeforeTest
  fun setUp() {
    db.start()
  }

  @AfterTest
  fun tearDown() {
    db.stop()
  }

  @Test
  fun `create returns the persisted record with normalized email`() =
    runBlocking {
      val record =
        repository.create(
          email = "User@Example.COM",
          passwordHash = "hash-1",
          firstName = "Jane",
          lastName = "Doe"
        )

      assertEquals("user@example.com", record.email)
      assertEquals("hash-1", record.passwordHash)
      assertEquals("Jane", record.firstName)
      assertEquals("Doe", record.lastName)
    }

  @Test
  fun `findByEmail returns the user when stored`() =
    runBlocking {
      val created = repository.create("u@x.com", "hash-1", "", "")

      val found = repository.findByEmail("u@x.com")

      assertEquals(created.id, found?.id)
      assertEquals("u@x.com", found?.email)
    }

  @Test
  fun `findByEmail is case-insensitive`() =
    runBlocking {
      repository.create("u@x.com", "hash-1", "", "")

      val found = repository.findByEmail("U@X.COM")

      assertNotNull(found)
      assertEquals("u@x.com", found.email)
    }

  @Test
  fun `findByEmail returns null for an unknown email`() =
    runBlocking {
      assertNull(repository.findByEmail("nobody@x.com"))
    }

  @Test
  fun `findById returns the user`() =
    runBlocking {
      val created = repository.create("u@x.com", "hash-1", "", "")

      val found = repository.findById(created.id)

      assertEquals(created, found)
    }

  @Test
  fun `findById returns null for an unknown id`() =
    runBlocking {
      assertNull(repository.findById(UUID.randomUUID()))
    }

  @Test
  fun `inserting a duplicate email maps to EmailAlreadyExists`() {
    runBlocking { repository.create("u@x.com", "hash-1", "", "") }

    val ex = assertFailsWith<AuthException> {
      runBlocking { repository.create("u@x.com", "hash-2", "", "") }
    }
    assertEquals(AuthError.EmailAlreadyExists, ex.error)
  }
}
