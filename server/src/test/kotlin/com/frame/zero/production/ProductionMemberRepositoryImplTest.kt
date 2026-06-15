package com.frame.zero.production

import com.frame.zero.auth.UserRepositoryImpl
import com.frame.zero.common.testing.PostgresTestDatabase
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProductionMemberRepositoryImplTest {
  private val db = PostgresTestDatabase()
  private val users = UserRepositoryImpl()
  private val productions = ProductionRepositoryImpl()
  private val members = ProductionMemberRepositoryImpl()

  @BeforeTest
  fun setUp() {
    db.start()
  }

  @AfterTest
  fun tearDown() {
    db.stop()
  }

  private var ownerSeq = 0

  private suspend fun newProduction(): UUID {
    val ownerId = users.create("owner${ownerSeq++}@x.com", "h", "Own", "Er").id
    return productions.create(
      title = "Film",
      genre = Genre.DRAMA,
      logline = null,
      phase = ProductionPhase.PRODUCTION,
      startDate = LocalDate(2026, 1, 1),
      wrapDate = LocalDate(2026, 12, 31),
      budgetCents = null,
      ownerUserId = ownerId
    ).id
  }

  @Test
  fun `add then findById returns the member`() =
    runBlocking {
      val productionId = newProduction()

      val added = members.add(productionId, null, "Ada Lovelace", "Director", "ada@x.com")
      val found = assertNotNull(members.findById(added.id))

      assertEquals("Ada Lovelace", found.name)
      assertEquals("Director", found.role)
      assertEquals("ada@x.com", found.email)
      assertEquals(productionId, found.productionId)
    }

  @Test
  fun `findByProduction returns only that production's members ordered by addedAt`() =
    runBlocking {
      val productionId = newProduction()
      val other = newProduction()
      val first = members.add(productionId, null, "First", "Grip", null)
      val second = members.add(productionId, null, "Second", "Gaffer", null)
      members.add(other, null, "Elsewhere", "PA", null)

      val result = members.findByProduction(productionId)

      assertEquals(listOf(first.id, second.id), result.map { it.id }, "ordered by insertion time")
    }

  @Test
  fun `isMember is true for a linked user and false otherwise`() =
    runBlocking {
      val productionId = newProduction()
      val userId = users.create("crew@x.com", "h", "Cr", "Ew").id
      val strangerId = users.create("stranger@x.com", "h", "St", "Ra").id
      members.add(productionId, userId, "Cr Ew", "Editor", "crew@x.com")

      assertTrue(members.isMember(userId, productionId))
      assertFalse(members.isMember(strangerId, productionId))
    }

  @Test
  fun `countByProduction counts members on that production`() =
    runBlocking {
      val productionId = newProduction()
      members.add(productionId, null, "A", "r", null)
      members.add(productionId, null, "B", "r", null)

      assertEquals(2, members.countByProduction(productionId))
    }

  @Test
  fun `countByProductions groups counts and omits productions with no members`() =
    runBlocking {
      val withTwo = newProduction()
      val withOne = newProduction()
      val withNone = newProduction()
      members.add(withTwo, null, "A", "r", null)
      members.add(withTwo, null, "B", "r", null)
      members.add(withOne, null, "C", "r", null)

      val counts = members.countByProductions(listOf(withTwo, withOne, withNone))

      assertEquals(2, counts[withTwo])
      assertEquals(1, counts[withOne])
      assertNull(counts[withNone], "a production with no members must be absent from the map")
    }

  @Test
  fun `countByProductions returns empty for an empty id list`() =
    runBlocking {
      assertTrue(members.countByProductions(emptyList()).isEmpty())
    }

  @Test
  fun `updateRole changes the role`() =
    runBlocking {
      val productionId = newProduction()
      val member = members.add(productionId, null, "Ada", "Grip", null)

      val updated = assertNotNull(members.updateRole(member.id, "Gaffer"))

      assertEquals("Gaffer", updated.role)
    }

  @Test
  fun `updateRole returns null for an unknown id`() =
    runBlocking {
      assertNull(members.updateRole(UUID.randomUUID(), "Grip"))
    }

  @Test
  fun `updateReportsTo sets and clears the manager link`() =
    runBlocking {
      val productionId = newProduction()
      val manager = members.add(productionId, null, "Boss", "Producer", null)
      val report = members.add(productionId, null, "Report", "Grip", null)

      val linked = assertNotNull(members.updateReportsTo(report.id, manager.id))
      assertEquals(manager.id, linked.reportsToMemberId)

      val unlinked = assertNotNull(members.updateReportsTo(report.id, null))
      assertNull(unlinked.reportsToMemberId)
    }

  @Test
  fun `remove deletes a member and reports success only when a row was removed`() =
    runBlocking {
      val productionId = newProduction()
      val member = members.add(productionId, null, "Ada", "Grip", null)

      assertTrue(members.remove(member.id))
      assertNull(members.findById(member.id))
      assertFalse(members.remove(member.id), "removing again must report no row deleted")
    }

  @Test
  fun `a member without a linked user has a null avatar color`() =
    runBlocking {
      val productionId = newProduction()

      val added = members.add(productionId, null, "Guest", "Extra", null)

      assertNull(added.avatarColorHex)
      assertNull(members.findById(added.id)?.avatarColorHex)
    }
}
