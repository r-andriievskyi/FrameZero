package com.frame.zero.task

import com.frame.zero.auth.UserRepositoryImpl
import com.frame.zero.common.testing.H2TestDatabase
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.production.ProductionMemberRepositoryImpl
import com.frame.zero.production.ProductionRepositoryImpl
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaskRepositoryImplTest {
  private val db = H2TestDatabase()
  private val users = UserRepositoryImpl()
  private val productions = ProductionRepositoryImpl()
  private val members = ProductionMemberRepositoryImpl()
  private val tasks = TaskRepositoryImpl()

  @BeforeTest
  fun setUp() {
    db.start()
  }

  @AfterTest
  fun tearDown() {
    db.stop()
  }

  private suspend fun newProduction(ownerId: UUID) =
    productions.create(
      title = "Film",
      genre = Genre.DRAMA,
      logline = null,
      phase = ProductionPhase.IDEA,
      startDate = LocalDate.of(2026, 1, 1),
      wrapDate = LocalDate.of(2026, 12, 31),
      budgetCents = null,
      ownerUserId = ownerId
    )

  @Test
  fun `findForUser does not leak tasks from productions the user cannot access`() =
    runBlocking {
      val ownerId = users.create("owner@x.com", "h", "Own", "Er").id
      val strangerId = users.create("stranger@x.com", "h", "Stra", "Nger").id
      val prod = newProduction(ownerId)
      tasks.create(prod.id, "Secret task", null, null, null)

      val (strangerTasks, _) = tasks.findForUser(strangerId, false, null, null, 20, null)
      assertTrue(strangerTasks.isEmpty(), "stranger must not see another production's tasks")

      val (ownerTasks, _) = tasks.findForUser(ownerId, false, null, null, 20, null)
      assertEquals(1, ownerTasks.size)
      assertEquals("Secret task", ownerTasks.first().title)
    }

  @Test
  fun `findForUser returns tasks for productions the user is a member of`() =
    runBlocking {
      val ownerId = users.create("owner@x.com", "h", "Own", "Er").id
      val memberId = users.create("member@x.com", "h", "Mem", "Ber").id
      val prod = newProduction(ownerId)
      tasks.create(prod.id, "Shared task", null, null, null)
      members.add(prod.id, memberId, "Mem Ber", "Grip", "member@x.com")

      val (memberTasks, _) = tasks.findForUser(memberId, false, null, null, 20, null)
      assertEquals(1, memberTasks.size)
      assertEquals("Shared task", memberTasks.first().title)
    }
}
