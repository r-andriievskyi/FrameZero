package com.frame.zero.common

import com.frame.zero.auth.UserRepositoryImpl
import com.frame.zero.common.testing.PostgresTestDatabase
import com.frame.zero.config.dbQuery
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Proves the real [ExposedTransactor] gives a service the atomic unit-of-work it relies on: every
 * repository write inside one `transaction { }` commits together, and a mid-block failure rolls
 * back **all** of them. Runs against real Postgres (Testcontainers) because rollback is a database
 * guarantee the in-memory [com.frame.zero.common.testing.NoopTransactor] cannot exercise.
 */
class ExposedTransactorRollbackTest {
  private val db = PostgresTestDatabase()
  private val transactor = ExposedTransactor()
  private val users = UserRepositoryImpl()

  @BeforeTest
  fun setUp() = db.start()

  @AfterTest
  fun tearDown() = db.stop()

  @Test
  fun `a failure mid-transaction rolls back every write in the block`() =
    runBlocking {
      val failure = assertFailsWith<IllegalStateException> {
        transactor.transaction {
          users.create("first@x.com", "hash", "First", "Writer")
          users.create("second@x.com", "hash", "Second", "Writer")
          // A service-method failure after partial writes — the whole unit must unwind.
          error("forced failure after two inserts")
        }
      }
      assertEquals("forced failure after two inserts", failure.message)

      // Neither insert may survive: the transaction is the boundary, not the individual call.
      assertNull(dbQuery { users.findByEmail("first@x.com") }, "first insert must roll back")
      assertNull(dbQuery { users.findByEmail("second@x.com") }, "second insert must roll back")
    }

  @Test
  fun `a transaction that returns normally commits its writes`() =
    runBlocking {
      val created = transactor.transaction {
        users.create("committed@x.com", "hash", "Com", "Mitted")
      }

      assertNotNull(dbQuery { users.findById(created.id) }, "a normally-returning block must commit")
      assertEquals("committed@x.com", created.email)
    }
}
