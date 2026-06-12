package com.frame.zero.dashboard

import com.frame.zero.auth.UserRepository
import com.frame.zero.common.Transactor
import com.frame.zero.dto.dashboard.DashboardResponse
import com.frame.zero.dto.dashboard.GreetingDto
import com.frame.zero.dto.dashboard.StatsDto
import com.frame.zero.dto.task.TaskSummaryDto
import com.frame.zero.production.ProductionRepository
import com.frame.zero.task.TaskRecord
import com.frame.zero.task.TaskRepository
import java.util.UUID

private const val DASHBOARD_TASKS_LIMIT = 3

class DashboardService(
  private val users: UserRepository,
  private val productions: ProductionRepository,
  private val tasks: TaskRepository,
  private val transactor: Transactor
) {
  suspend fun get(userId: UUID): DashboardResponse =
    transactor.transaction {
      val user = users.findById(userId)
      val displayName = user?.let { "${it.firstName} ${it.lastName}".trim() }.orEmpty()

      val activeCount = productions.countActiveForUser(userId)
      val openTaskCount = tasks.countOpenForUser(userId)

      val myTasks = tasks.findForUserLimit(userId, DASHBOARD_TASKS_LIMIT).map { it.toSummaryDto() }

      DashboardResponse(
        greeting = GreetingDto(
          displayName = displayName,
          activeProductionsCount = activeCount,
          openTasksCount = openTaskCount
        ),
        stats = StatsDto(activeProjects = activeCount, openTasks = openTaskCount),
        myTasks = myTasks
      )
    }

  private fun TaskRecord.toSummaryDto(): TaskSummaryDto =
    TaskSummaryDto(
      id = id.toString(),
      title = title,
      productionTitle = productionTitle,
      dueDate = dueDate,
      status = status
    )
}
