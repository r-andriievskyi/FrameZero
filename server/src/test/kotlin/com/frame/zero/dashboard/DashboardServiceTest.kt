package com.frame.zero.dashboard

import com.frame.zero.auth.testing.FakeUserRepository
import com.frame.zero.common.testing.NoopTransactor
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.production.testing.FakeProductionRepository
import com.frame.zero.task.testing.FakeTaskRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class DashboardServiceTest {
  private val users = FakeUserRepository()
  private val productions = FakeProductionRepository()
  private val tasks = FakeTaskRepository()
  private val service = DashboardService(users, productions, tasks, NoopTransactor())

  private suspend fun activeProductionOwnedBy(userId: UUID) {
    productions.create(
      title = "Film",
      genre = Genre.DRAMA,
      logline = null,
      phase = ProductionPhase.PRODUCTION,
      startDate = LocalDate(2026, 1, 1),
      wrapDate = LocalDate(2026, 12, 31),
      budgetCents = null,
      ownerUserId = userId
    )
  }

  @Test
  fun `get assembles the display name, aggregated stats, and the top open tasks`() =
    runTest {
      val user = users.create("u@x.com", "h", "Ada", "Lovelace")
      activeProductionOwnedBy(user.id)
      activeProductionOwnedBy(user.id)
      // Four open tasks assigned to the user — only the first three may surface.
      (1..4).forEach { tasks.create(UUID.randomUUID(), "t$it", null, LocalDate(2026, 4, it), user.id) }

      val response = service.get(user.id)

      assertEquals("Ada Lovelace", response.greeting.displayName)
      assertEquals(2, response.greeting.activeProductionsCount)
      assertEquals(4, response.greeting.openTasksCount)
      assertEquals(2, response.stats.activeProjects)
      assertEquals(4, response.stats.openTasks)
      assertEquals(3, response.myTasks.size, "dashboard surfaces at most three tasks")
      val first = response.myTasks.first()
      assertEquals("t1", first.title)
      assertEquals(LocalDate(2026, 4, 1), first.dueDate)
      assertEquals(TaskStatus.OPEN, first.status)
    }

  @Test
  fun `get trims the trailing space when the last name is blank`() =
    runTest {
      val user = users.create("u@x.com", "h", "Ada", "")

      val response = service.get(user.id)

      assertEquals("Ada", response.greeting.displayName)
    }

  @Test
  fun `get yields an empty display name and zero stats for an unknown user`() =
    runTest {
      val response = service.get(UUID.randomUUID())

      assertEquals("", response.greeting.displayName)
      assertEquals(0, response.stats.activeProjects)
      assertEquals(0, response.stats.openTasks)
      assertEquals(0, response.myTasks.size)
    }

  @Test
  fun `get counts only the requesting user's open tasks`() =
    runTest {
      val user = users.create("u@x.com", "h", "Ada", "Lovelace")
      val other = users.create("other@x.com", "h", "Bo", "Diddley")
      tasks.create(UUID.randomUUID(), "mine", null, null, user.id)
      tasks.create(UUID.randomUUID(), "theirs", null, null, other.id)

      val response = service.get(user.id)

      assertEquals(1, response.stats.openTasks)
      assertEquals(listOf("mine"), response.myTasks.map { it.title })
    }
}
