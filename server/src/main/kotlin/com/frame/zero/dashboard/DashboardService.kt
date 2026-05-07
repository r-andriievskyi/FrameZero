package com.frame.zero.dashboard

import com.frame.zero.auth.UserRepository
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

private const val DASHBOARD_PRODUCTIONS_LIMIT = 5

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

    val productionStatus = productionItems.map { prod ->
      val today = java.time.LocalDate.now()
      val progress = computeProgress(prod.startDate, prod.wrapDate, today)
      val daysLeft = ChronoUnit.DAYS
        .between(today, prod.wrapDate)
        .toInt()
      val membersCount = members.countByProduction(prod.id)
      ProductionSummaryDto(
        id = prod.id.toString(),
        title = prod.title,
        genre = prod.genre,
        phase = prod.phase,
        progressPercent = progress,
        daysLeft = daysLeft,
        membersCount = membersCount,
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

  private companion object {
    fun computeProgress(
      start: java.time.LocalDate,
      wrap: java.time.LocalDate,
      today: java.time.LocalDate
    ): Int {
      if (!today.isAfter(start)) return 0
      if (!today.isBefore(wrap)) return 100
      val total = ChronoUnit.DAYS
        .between(start, wrap)
        .coerceAtLeast(1)
      val elapsed = ChronoUnit.DAYS
        .between(start, today)
      return (elapsed * 100 / total).toInt().coerceIn(0, 100)
    }
  }
}
