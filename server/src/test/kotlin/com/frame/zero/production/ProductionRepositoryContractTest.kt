package com.frame.zero.production

import com.frame.zero.auth.UserRepositoryImpl
import com.frame.zero.common.testing.PostgresTestDatabase
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.ProductionSort
import com.frame.zero.production.testing.FakeProductionMemberRepository
import com.frame.zero.production.testing.FakeProductionRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * One set of access-semantics scenarios run against **both** the in-memory fake and the real
 * Postgres implementation. The route and service tests lean on the fake, so if the fake's
 * notion of "which productions can this user see" drifts from the SQL, those tests give false
 * confidence. Anything that passes here is guaranteed to behave identically on both sides.
 *
 * Scope is deliberately the access/visibility/count contract (owner OR member, minus soft-deleted)
 * — the surface where the fake and the SQL EXISTS-join can disagree. Sort/cursor/search specifics
 * are SQL-only and live in [ProductionRepositoryImplTest].
 */
abstract class ProductionRepositoryContract {
  protected lateinit var productions: ProductionRepository
  protected lateinit var members: ProductionMemberRepository

  /** Real impl starts a DB here; the fake does nothing. */
  protected open fun onSetUp() {}

  protected open fun onTearDown() {}

  protected abstract fun createRepositories(): Pair<ProductionRepository, ProductionMemberRepository>

  /** A persisted user on the real impl; any UUID on the fake (no FK to satisfy). */
  protected abstract suspend fun newUserId(email: String): UUID

  @BeforeTest
  fun baseSetUp() {
    onSetUp()
    val (p, m) = createRepositories()
    productions = p
    members = m
  }

  @AfterTest
  fun baseTearDown() {
    onTearDown()
  }

  private suspend fun createProduction(
    ownerId: UUID,
    title: String = "Film",
    phase: ProductionPhase = ProductionPhase.PRODUCTION
  ): UUID =
    productions.create(
      title = title,
      genre = Genre.DRAMA,
      logline = null,
      phase = phase,
      startDate = LocalDate(2026, 1, 1),
      wrapDate = LocalDate(2026, 12, 31),
      budgetCents = null,
      ownerUserId = ownerId
    ).id

  private suspend fun accessibleIds(userId: UUID): Set<UUID> =
    productions
      .findAccessible(userId, emptyList(), null, ProductionSort.DUE_DATE, 50, null)
      .first
      .map { it.id }
      .toSet()

  @Test
  fun `the owner can see their own production`() =
    runBlocking {
      val ownerId = newUserId("owner@x.com")
      val prod = createProduction(ownerId)

      assertEquals(setOf(prod), accessibleIds(ownerId))
    }

  @Test
  fun `a member who is not the owner can see the production`() =
    runBlocking {
      val ownerId = newUserId("owner@x.com")
      val memberId = newUserId("member@x.com")
      val prod = createProduction(ownerId)
      members.add(prod, memberId, "Mem Ber", "Grip", "member@x.com")

      assertEquals(setOf(prod), accessibleIds(memberId), "membership must grant visibility")
    }

  @Test
  fun `a stranger sees no productions`() =
    runBlocking {
      val ownerId = newUserId("owner@x.com")
      val strangerId = newUserId("stranger@x.com")
      createProduction(ownerId)

      assertTrue(accessibleIds(strangerId).isEmpty())
    }

  @Test
  fun `soft-deleted productions are excluded`() =
    runBlocking {
      val ownerId = newUserId("owner@x.com")
      val live = createProduction(ownerId, title = "Live")
      val doomed = createProduction(ownerId, title = "Doomed")
      productions.softDelete(doomed)

      assertEquals(setOf(live), accessibleIds(ownerId))
    }

  @Test
  fun `countActiveForUser unions owned and member productions and drops soft-deleted`() =
    runBlocking {
      val ownerId = newUserId("owner@x.com")
      val userId = newUserId("user@x.com")
      val viaMembership = createProduction(ownerId, title = "Owned by other")
      members.add(viaMembership, userId, "Us Er", "Grip", "user@x.com")
      createProduction(userId, title = "Owned by user")
      val deleted = createProduction(userId, title = "Deleted")
      productions.softDelete(deleted)

      assertEquals(2, productions.countActiveForUser(userId), "one via membership, one via ownership")
    }
}

class FakeProductionRepositoryContractTest : ProductionRepositoryContract() {
  override fun createRepositories(): Pair<ProductionRepository, ProductionMemberRepository> {
    val members = FakeProductionMemberRepository()
    return FakeProductionRepository(members) to members
  }

  override suspend fun newUserId(email: String): UUID = UUID.randomUUID()
}

class PostgresProductionRepositoryContractTest : ProductionRepositoryContract() {
  private val db = PostgresTestDatabase()
  private val users = UserRepositoryImpl()

  override fun onSetUp() = db.start()

  override fun onTearDown() = db.stop()

  override fun createRepositories(): Pair<ProductionRepository, ProductionMemberRepository> =
    ProductionRepositoryImpl() to ProductionMemberRepositoryImpl()

  override suspend fun newUserId(email: String): UUID = users.create(email, "hash", "Us", "Er").id
}
