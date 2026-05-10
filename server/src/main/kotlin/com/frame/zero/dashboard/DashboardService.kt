package com.frame.zero.dashboard

import com.frame.zero.auth.UserRepository
import com.frame.zero.common.computeProgressPercent
import com.frame.zero.common.dueLabelFor
import com.frame.zero.common.toKotlin
import com.frame.zero.domain.production.ProductionSort
import com.frame.zero.dto.dashboard.DashboardResponse
import com.frame.zero.dto.dashboard.GreetingDto
import com.frame.zero.dto.dashboard.StatsDto
import com.frame.zero.dto.production.ProductionSummaryDto
import com.frame.zero.dto.task.TaskSummaryDto
import com.frame.zero.production.ProductionMemberRepository
import com.frame.zero.production.ProductionRepository
import com.frame.zero.task.TaskRecord
import com.frame.zero.task.TaskRepository
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.time.toKotlinInstant

private const val DASHBOARD_PRODUCTIONS_LIMIT = 3

class DashboardService(
  private val users: UserRepository,
  private val productions: ProductionRepository,
  private val members: ProductionMemberRepository,
  private val tasks: TaskRepository
) {
  suspend fun get(
    userId: UUID,
    timezone: ZoneId
  ): DashboardResponse {
    val user = users.findById(userId)
    val displayName = user?.let { "${it.firstName} ${it.lastName}".trim() } ?: ""

    val activeCount = productions.countActiveForUser(userId)
    val openTaskCount = tasks.countOpenForUser(userId)

    val myTasks = tasks.findForUserLimit(userId, DASHBOARD_PRODUCTIONS_LIMIT).map { it.toSummaryDto(timezone) }

    val (productionItems, _) = productions.findAccessible(
      userId = userId,
      phases = emptyList(),
      query = null,
      sort = ProductionSort.DUE_DATE,
      limit = DASHBOARD_PRODUCTIONS_LIMIT,
      cursor = null
    )

    val today = java.time.LocalDate.now()
    val productionIds = productionItems.map { it.id }
    val membersCounts = members.countByProductions(productionIds)

    val productionStatus = productionItems.map { prod ->
      val progress = computeProgressPercent(prod.startDate, prod.wrapDate, today)
      val daysLeft = ChronoUnit.DAYS
        .between(today, prod.wrapDate)
        .toInt()
      ProductionSummaryDto(
        id = prod.id.toString(),
        title = prod.title,
        genre = prod.genre,
        phase = prod.phase,
        progressPercent = progress,
        daysLeft = daysLeft,
        membersCount = membersCounts[prod.id] ?: 0,
        updatedAt = prod.updatedAt.toKotlinInstant()
      )
    }

    return DashboardResponse(
      greeting =
        GreetingDto(
          displayName = displayName,
          activeProductionsCount = activeCount,
          openTasksCount = openTaskCount
        ),
      stats = StatsDto(activeProjects = activeCount, openTasks = openTaskCount),
      myTasks = myTasks,
      productionStatus = productionStatus
    )
  }

  private fun TaskRecord.toSummaryDto(tz: ZoneId): TaskSummaryDto {
    val label = dueDate?.let { dueLabelFor(it, tz) }
    return TaskSummaryDto(
      id = id.toString(),
      title = title,
      productionTitle = productionTitle,
      dueDate = dueDate?.toKotlin(),
      dueLabel = label,
      status = status
    )
  }
}
