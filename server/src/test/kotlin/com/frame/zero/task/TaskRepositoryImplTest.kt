package com.frame.zero.task

import com.frame.zero.auth.UserRepositoryImpl
import com.frame.zero.common.testing.PostgresTestDatabase
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.production.ProductionMemberRepositoryImpl
import com.frame.zero.production.ProductionRepositoryImpl
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaskRepositoryImplTest {
  private val db = PostgresTestDatabase()
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
      startDate = LocalDate(2026, 1, 1),
      wrapDate = LocalDate(2026, 12, 31),
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

  @Test
  fun `findForUser pages without skips or duplicates when creation order differs from due-date order`() =
    runBlocking {
      val ownerId = users.create("owner@x.com", "h", "Own", "Er").id
      val prod = newProduction(ownerId)
      // Insert in an order unrelated to due dates, including a task without one;
      // the cursor must follow the (dueDate, id) sort, not creation time.
      tasks.create(prod.id, "due-3", null, LocalDate(2026, 3, 3), null)
      tasks.create(prod.id, "due-1", null, LocalDate(2026, 1, 1), null)
      tasks.create(prod.id, "no-due", null, null, null)
      tasks.create(prod.id, "due-2", null, LocalDate(2026, 2, 2), null)

      val collected = mutableListOf<String>()
      var cursor: String? = null
      do {
        val (page, next) = tasks.findForUser(ownerId, false, null, null, 2, cursor)
        collected += page.map { it.title }
        cursor = next
      } while (cursor != null)

      assertEquals(listOf("due-1", "due-2", "due-3", "no-due"), collected)
    }

  @Test
  fun `tasks of soft-deleted productions are excluded from every user-facing query`() =
    runBlocking {
      val ownerId = users.create("owner@x.com", "h", "Own", "Er").id
      val prod = newProduction(ownerId)
      members.add(prod.id, ownerId, "Own Er", "Owner", "owner@x.com")
      tasks.create(prod.id, "Doomed", null, LocalDate(2026, 6, 1), ownerId)
      productions.softDelete(prod.id)

      assertTrue(tasks.findForUserLimit(ownerId, 10).isEmpty(), "dashboard list must be empty")
      assertEquals(0, tasks.countOpenForUser(ownerId), "dashboard count must be zero")
      assertTrue(
        tasks
          .findInRangeForUser(ownerId, LocalDate(2026, 1, 1), LocalDate(2026, 12, 31))
          .isEmpty(),
        "schedule range query must be empty"
      )
      val (page, _) = tasks.findForUser(ownerId, false, null, null, 20, null)
      assertTrue(page.isEmpty(), "task list must be empty")
    }
}
