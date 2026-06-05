package com.frame.zero.domain.dashboard

import com.frame.zero.dto.dashboard.DashboardResponse
import com.frame.zero.dto.dashboard.GreetingDto
import com.frame.zero.dto.dashboard.StatsDto
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.dto.task.TaskSummaryDto
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DashboardMapperTest {
  @Test
  fun `response toDomain uses greeting displayName and maps stats and tasks`() {
    val response = DashboardResponse(
      greeting = GreetingDto(displayName = "Ada", activeProductionsCount = 2, openTasksCount = 5),
      stats = StatsDto(activeProjects = 2, openTasks = 5),
      myTasks = listOf(
        TaskSummaryDto(
          id = "t1",
          title = "Storyboard",
          productionTitle = "Pilot",
          dueDate = LocalDate(2026, 4, 26),
          status = TaskStatus.OPEN
        )
      )
    )

    val dashboard = response.toDomain()

    assertEquals("Ada", dashboard.displayName)
    assertEquals(DashboardStats(activeProjects = 2, openTasks = 5), dashboard.stats)
    assertEquals(1, dashboard.myTasks.size)
    val task = dashboard.myTasks.single()
    assertEquals(
      DashboardTask(
        id = "t1",
        title = "Storyboard",
        productionTitle = "Pilot",
        dueDate = LocalDate(2026, 4, 26),
        status = TaskStatus.OPEN
      ),
      task
    )
  }

  @Test
  fun `task toDomain keeps null due date`() {
    val dto = TaskSummaryDto(
      id = "t2",
      title = "Casting",
      productionTitle = "Pilot",
      dueDate = null,
      status = TaskStatus.DONE
    )

    assertEquals(null, dto.toDomain().dueDate)
    assertEquals(TaskStatus.DONE, dto.toDomain().status)
  }
}
