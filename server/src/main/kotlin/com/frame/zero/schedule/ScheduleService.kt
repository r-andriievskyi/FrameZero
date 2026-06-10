package com.frame.zero.schedule

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.toKotlin
import com.frame.zero.dto.schedule.CreateScheduleEventRequest
import com.frame.zero.dto.schedule.ScheduleDayDto
import com.frame.zero.dto.schedule.ScheduleEventDto
import com.frame.zero.dto.schedule.ScheduleResponse
import com.frame.zero.dto.schedule.ScheduleTaskDto
import com.frame.zero.dto.schedule.UpdateScheduleEventRequest
import com.frame.zero.production.AccessLevel
import com.frame.zero.production.ProductionAccessService
import com.frame.zero.task.TaskRecord
import com.frame.zero.task.TaskRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

class ScheduleService(
  private val events: ScheduleEventRepository,
  private val tasks: TaskRepository,
  private val access: ProductionAccessService
) {
  suspend fun get(
    userId: UUID,
    view: String,
    dateParam: String,
    timezone: ZoneId
  ): ScheduleResponse {
    val (rangeStart, rangeEnd) =
      when (view) {
        "day" -> {
          val date = parseDate(dateParam)
          Pair(date, date)
        }
        "week" -> {
          val date = parseDate(dateParam)
          val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
          val sunday = monday.plusDays(6)
          Pair(monday, sunday)
        }
        "month" -> {
          val ym = parseYearMonth(dateParam)
          Pair(ym.atDay(1), ym.atEndOfMonth())
        }
        else ->
          throw AppException(
            AppError.ValidationError(mapOf("view" to "Must be day, week, or month"))
          )
      }

    val rangeStartInstant = rangeStart.atStartOfDay(timezone).toInstant()
    val rangeEndInstant = rangeEnd.plusDays(1).atStartOfDay(timezone).toInstant()

    val eventRecords = events.findInRangeForUser(userId, rangeStartInstant, rangeEndInstant)
    val taskRecords = tasks.findInRangeForUser(userId, rangeStart, rangeEnd)

    val days =
      generateDays(rangeStart, rangeEnd).map { date ->
        val dayStart = date.atStartOfDay(timezone).toInstant()
        val dayEnd = date.plusDays(1).atStartOfDay(timezone).toInstant()
        val dayEvents =
          eventRecords
            .filter { it.startsAt >= dayStart && it.startsAt < dayEnd }
            .map { it.toDto() }
        val dayTasks = taskRecords.filter { it.dueDate == date }.map { it.toScheduleTaskDto() }
        ScheduleDayDto(date = date.toKotlin(), events = dayEvents, tasks = dayTasks)
      }

    return ScheduleResponse(
      rangeStart = rangeStart.toKotlin(),
      rangeEnd = rangeEnd.toKotlin(),
      days = days
    )
  }

  suspend fun create(
    userId: UUID,
    request: CreateScheduleEventRequest
  ): ScheduleEventDto {
    val errors = mutableMapOf<String, String>()
    if (request.title.isBlank()) errors["title"] = "Required"
    if (request.title.length > MAX_TITLE_LENGTH) {
      errors["title"] = "Max $MAX_TITLE_LENGTH characters"
    }
    if ((request.location?.length ?: 0) > MAX_LOCATION_LENGTH) {
      errors["location"] = "Max $MAX_LOCATION_LENGTH characters"
    }
    val productionId =
      runCatching { UUID.fromString(request.productionId) }.getOrNull()
        ?: run {
          errors["productionId"] = "Invalid UUID"
          null
        }
    if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))

    access.requireAccess(userId, productionId!!, AccessLevel.WRITE)

    if (request.endsAt <= request.startsAt) {
      throw AppException(AppError.ValidationError(mapOf("endsAt" to "Must be after startsAt")))
    }

    val record =
      events.create(
        productionId = productionId,
        title = request.title.trim(),
        location = request.location?.trim(),
        startsAt = request.startsAt.toJavaInstant(),
        endsAt = request.endsAt.toJavaInstant(),
        kind = request.kind
      )
    return record.toDto()
  }

  suspend fun update(
    userId: UUID,
    eventId: UUID,
    request: UpdateScheduleEventRequest
  ): ScheduleEventDto {
    val event = events.findById(eventId) ?: throw AppException(AppError.NotFound)
    access.requireAccess(userId, event.productionId, AccessLevel.WRITE)

    val errors = mutableMapOf<String, String>()
    val requestTitle = request.title
    if (requestTitle != null && requestTitle.isBlank()) errors["title"] = "Cannot be empty"
    if (requestTitle != null && requestTitle.length > MAX_TITLE_LENGTH) {
      errors["title"] = "Max $MAX_TITLE_LENGTH characters"
    }
    if ((request.location?.length ?: 0) > MAX_LOCATION_LENGTH) {
      errors["location"] = "Max $MAX_LOCATION_LENGTH characters"
    }
    if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))

    val startsAt = request.startsAt?.toJavaInstant()
    val endsAt = request.endsAt?.toJavaInstant()
    if (startsAt != null && endsAt != null && endsAt <= startsAt) {
      throw AppException(AppError.ValidationError(mapOf("endsAt" to "Must be after startsAt")))
    }

    val updated =
      events.update(
        id = eventId,
        title = request.title?.trim(),
        location = request.location?.trim(),
        startsAt = startsAt,
        endsAt = endsAt,
        kind = request.kind
      ) ?: throw AppException(AppError.NotFound)
    return updated.toDto()
  }

  suspend fun delete(
    userId: UUID,
    eventId: UUID
  ) {
    val event = events.findById(eventId) ?: throw AppException(AppError.NotFound)
    access.requireAccess(userId, event.productionId, AccessLevel.WRITE)
    events.delete(eventId)
  }

  private fun parseDate(value: String): LocalDate =
    runCatching { LocalDate.parse(value) }.getOrElse {
      throw AppException(AppError.ValidationError(mapOf("date" to "Must be an ISO date (yyyy-MM-dd)")))
    }

  private fun parseYearMonth(value: String): YearMonth =
    runCatching { YearMonth.parse(value) }.getOrElse {
      throw AppException(AppError.ValidationError(mapOf("date" to "Must be an ISO month (yyyy-MM)")))
    }

  private fun generateDays(
    start: LocalDate,
    end: LocalDate
  ): List<LocalDate> {
    val days = mutableListOf<LocalDate>()
    var current = start
    while (!current.isAfter(end)) {
      days += current
      current = current.plusDays(1)
    }
    return days
  }

  private fun ScheduleEventRecord.toDto(): ScheduleEventDto =
    ScheduleEventDto(
      id = id.toString(),
      title = title,
      location = location,
      startsAt = startsAt.toKotlinInstant(),
      endsAt = endsAt.toKotlinInstant(),
      kind = kind,
      productionId = productionId.toString(),
      productionTitle = productionTitle
    )

  private fun TaskRecord.toScheduleTaskDto(): ScheduleTaskDto =
    ScheduleTaskDto(
      id = id.toString(),
      title = title,
      productionId = productionId.toString(),
      productionTitle = productionTitle,
      dueDate = requireNotNull(dueDate) { "Schedule task must have a due date" }.toKotlin(),
      status = status,
      priority = priority
    )

  private companion object {
    // Match the column sizes in ScheduleEventsTable.
    const val MAX_TITLE_LENGTH = 200
    const val MAX_LOCATION_LENGTH = 300
  }
}
