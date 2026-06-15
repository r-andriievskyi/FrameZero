package com.frame.zero.production

import com.frame.zero.auth.UserRepositoryImpl
import com.frame.zero.common.testing.PostgresTestDatabase
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.ProductionSort
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProductionRepositoryImplTest {
  private val db = PostgresTestDatabase()
  private val users = UserRepositoryImpl()
  private val members = ProductionMemberRepositoryImpl()
  private val productions = ProductionRepositoryImpl()

  @BeforeTest
  fun setUp() {
    db.start()
  }

  @AfterTest
  fun tearDown() {
    db.stop()
  }

  private suspend fun newOwner(email: String = "owner@x.com"): UUID = users.create(email, "hash", "Own", "Er").id

  private suspend fun newProduction(
    ownerId: UUID,
    title: String = "Film",
    phase: ProductionPhase = ProductionPhase.PRODUCTION,
    wrapDate: LocalDate = LocalDate(2026, 12, 31),
    logline: String? = "Original logline"
  ) = productions.create(
    title = title,
    genre = Genre.DRAMA,
    logline = logline,
    phase = phase,
    startDate = LocalDate(2026, 1, 1),
    wrapDate = wrapDate,
    budgetCents = null,
    ownerUserId = ownerId
  )

  @Test
  fun `findById returns an active production and null after soft delete`() =
    runBlocking {
      val ownerId = newOwner()
      val prod = newProduction(ownerId, title = "Pilot")

      assertEquals("Pilot", productions.findById(prod.id)?.title)

      productions.softDelete(prod.id)
      assertNull(productions.findById(prod.id), "soft-deleted production must not be returned")
    }

  @Test
  fun `findAccessible returns productions the user owns`() =
    runBlocking {
      val ownerId = newOwner()
      val prod = newProduction(ownerId)

      val (items, _) = productions.findAccessible(ownerId, emptyList(), null, ProductionSort.DUE_DATE, 20, null)

      assertEquals(listOf(prod.id), items.map { it.id })
    }

  @Test
  fun `findAccessible includes productions the user is a member of but does not own`() =
    runBlocking {
      val ownerId = newOwner("owner@x.com")
      val memberId = users.create("member@x.com", "h", "Mem", "Ber").id
      val prod = newProduction(ownerId)
      members.add(prod.id, memberId, "Mem Ber", "Grip", "member@x.com")

      val (items, _) = productions.findAccessible(memberId, emptyList(), null, ProductionSort.DUE_DATE, 20, null)

      assertEquals(
        listOf(prod.id),
        items.map { it.id },
        "a member who is not the owner must still see the production"
      )
    }

  @Test
  fun `findAccessible excludes productions the user neither owns nor is a member of`() =
    runBlocking {
      val ownerId = newOwner("owner@x.com")
      val strangerId = users.create("stranger@x.com", "h", "Stra", "Nger").id
      newProduction(ownerId)

      val (items, _) = productions.findAccessible(strangerId, emptyList(), null, ProductionSort.DUE_DATE, 20, null)

      assertTrue(items.isEmpty(), "stranger must not see another user's production")
    }

  @Test
  fun `findAccessible excludes soft-deleted productions`() =
    runBlocking {
      val ownerId = newOwner()
      val live = newProduction(ownerId, title = "Live")
      val doomed = newProduction(ownerId, title = "Doomed")
      productions.softDelete(doomed.id)

      val (items, _) = productions.findAccessible(ownerId, emptyList(), null, ProductionSort.DUE_DATE, 20, null)

      assertEquals(listOf(live.id), items.map { it.id })
    }

  @Test
  fun `findAccessible filters by phase`() =
    runBlocking {
      val ownerId = newOwner()
      val inProd = newProduction(ownerId, title = "Shooting", phase = ProductionPhase.PRODUCTION)
      newProduction(ownerId, title = "Idea", phase = ProductionPhase.IDEA)

      val (items, _) =
        productions.findAccessible(
          ownerId,
          listOf(ProductionPhase.PRODUCTION),
          null,
          ProductionSort.DUE_DATE,
          20,
          null
        )

      assertEquals(listOf(inProd.id), items.map { it.id })
    }

  @Test
  fun `findAccessible matches the title query case-insensitively`() =
    runBlocking {
      val ownerId = newOwner()
      val match = newProduction(ownerId, title = "Neon Wolves")
      newProduction(ownerId, title = "Echoes of Silence")

      val (items, _) =
        productions.findAccessible(ownerId, emptyList(), "neon", ProductionSort.DUE_DATE, 20, null)

      assertEquals(listOf(match.id), items.map { it.id })
    }

  @Test
  fun `findAccessible treats LIKE wildcards in the query as literal characters`() =
    runBlocking {
      val ownerId = newOwner()
      // The literal title has no underscore; "a_b" must not match it via the SQL
      // single-char wildcard, or the query is injectable / over-matching.
      newProduction(ownerId, title = "axb")

      val (wildcard, _) =
        productions.findAccessible(ownerId, emptyList(), "a_b", ProductionSort.DUE_DATE, 20, null)
      assertTrue(wildcard.isEmpty(), "underscore must be escaped, not treated as a wildcard")

      val (literal, _) =
        productions.findAccessible(ownerId, emptyList(), "axb", ProductionSort.DUE_DATE, 20, null)
      assertEquals(1, literal.size, "an exact substring must still match")
    }

  @Test
  fun `findAccessible paginates by due date then id with no skips or duplicates`() =
    runBlocking {
      val ownerId = newOwner()
      // Insert in an order unrelated to wrap date; the cursor must follow the sort.
      newProduction(ownerId, title = "w3", wrapDate = LocalDate(2026, 3, 3))
      newProduction(ownerId, title = "w1", wrapDate = LocalDate(2026, 1, 1))
      newProduction(ownerId, title = "w4", wrapDate = LocalDate(2026, 4, 4))
      newProduction(ownerId, title = "w2", wrapDate = LocalDate(2026, 2, 2))

      val collected = mutableListOf<String>()
      var cursor: String? = null
      do {
        val (page, next) = productions.findAccessible(ownerId, emptyList(), null, ProductionSort.DUE_DATE, 2, cursor)
        collected += page.map { it.title }
        cursor = next
      } while (cursor != null)

      assertEquals(listOf("w1", "w2", "w3", "w4"), collected)
    }

  @Test
  fun `findAccessible RECENT pagination covers every item exactly once`() =
    runBlocking {
      val ownerId = newOwner()
      val ids = (1..5).map { newProduction(ownerId, title = "p$it").id }.toSet()

      val collected = mutableListOf<UUID>()
      var cursor: String? = null
      do {
        val (page, next) = productions.findAccessible(ownerId, emptyList(), null, ProductionSort.RECENT, 2, cursor)
        collected += page.map { it.id }
        cursor = next
      } while (cursor != null)

      assertEquals(ids, collected.toSet(), "every accessible production must appear")
      assertEquals(ids.size, collected.size, "no production may be paged twice")
    }

  @Test
  fun `countActiveForUser excludes distribution archived and soft-deleted`() =
    runBlocking {
      val ownerId = newOwner()
      newProduction(ownerId, title = "active", phase = ProductionPhase.PRODUCTION)
      newProduction(ownerId, title = "idea", phase = ProductionPhase.IDEA)
      newProduction(ownerId, title = "distributing", phase = ProductionPhase.DISTRIBUTION)
      newProduction(ownerId, title = "archived", phase = ProductionPhase.ARCHIVED)
      val deleted = newProduction(ownerId, title = "deleted", phase = ProductionPhase.PRODUCTION)
      productions.softDelete(deleted.id)

      assertEquals(2, productions.countActiveForUser(ownerId), "active + idea only")
    }

  @Test
  fun `countActiveForUser counts productions the user is a member of`() =
    runBlocking {
      val ownerId = newOwner("owner@x.com")
      val memberId = users.create("member@x.com", "h", "Mem", "Ber").id
      val prod = newProduction(ownerId, phase = ProductionPhase.PRODUCTION)
      members.add(prod.id, memberId, "Mem Ber", "Grip", "member@x.com")

      assertEquals(1, productions.countActiveForUser(memberId))
    }

  @Test
  fun `update mutates only the provided fields`() =
    runBlocking {
      val ownerId = newOwner()
      val prod = newProduction(ownerId, title = "Before", logline = "Original logline")

      val updated = assertNotNull(productions.update(prod.id, title = "After", null, null, null, null))

      assertEquals("After", updated.title)
      assertEquals("Original logline", updated.logline, "an omitted field must be left untouched")
      assertEquals(prod.genre, updated.genre)
    }

  @Test
  fun `update returns null for an unknown id`() =
    runBlocking {
      assertNull(productions.update(UUID.randomUUID(), "x", null, null, null, null))
    }

  @Test
  fun `updatePhase changes the phase`() =
    runBlocking {
      val ownerId = newOwner()
      val prod = newProduction(ownerId, phase = ProductionPhase.IDEA)

      val updated = assertNotNull(productions.updatePhase(prod.id, ProductionPhase.PRODUCTION))

      assertEquals(ProductionPhase.PRODUCTION, updated.phase)
    }
}
