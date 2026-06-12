package com.frame.zero.schedule

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.Transactor
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
import java.util.UUID
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class ScheduleService(
  private val events: ScheduleEventRepository,
  private val tasks: TaskRepository,
  private val access: ProductionAccessService,
  private val transactor: Transactor
) {
  suspend fun get(
    userId: UUID,
    view: String,
    dateParam: String,
    timezone: TimeZone
  ): ScheduleResponse =
    transactor.transaction {
      val (rangeStart, rangeEnd) =
        when (view) {
          "day" -> {
            val date = parseDate(dateParam)
            date to date
          }
          "week" -> {
            val date = parseDate(dateParam)
            val monday = date.minus(date.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
            val sunday = monday.plus(6, DateTimeUnit.DAY)
            monday to sunday
          }
          "month" -> monthRange(dateParam)
          else ->
            throw AppException(
              AppError.ValidationError(mapOf("view" to "Must be day, week, or month"))
            )
        }

      val rangeStartInstant = rangeStart.atStartOfDayIn(timezone)
      val rangeEndInstant = rangeEnd.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timezone)

      val eventRecords = events.findInRangeForUser(userId, rangeStartInstant, rangeEndInstant)
      val taskRecords = tasks.findInRangeForUser(userId, rangeStart, rangeEnd)

      val days =
        generateDays(rangeStart, rangeEnd).map { date ->
          val dayStart = date.atStartOfDayIn(timezone)
          val dayEnd = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timezone)
          val dayEvents =
            eventRecords
              .filter { it.startsAt >= dayStart && it.startsAt < dayEnd }
              .map { it.toDto() }
          val dayTasks = taskRecords.filter { it.dueDate == date }.map { it.toScheduleTaskDto() }
          ScheduleDayDto(date = date, events = dayEvents, tasks = dayTasks)
        }

      ScheduleResponse(
        rangeStart = rangeStart,
        rangeEnd = rangeEnd,
        days = days
      )
    }

  suspend fun create(
    userId: UUID,
    request: CreateScheduleEventRequest
  ): ScheduleEventDto =
    transactor.transaction {
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
          startsAt = request.startsAt,
          endsAt = request.endsAt,
          kind = request.kind
        )
      record.toDto()
    }

  suspend fun update(
    userId: UUID,
    eventId: UUID,
    request: UpdateScheduleEventRequest
  ): ScheduleEventDto =
    transactor.transaction {
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

      val startsAt = request.startsAt
      val endsAt = request.endsAt
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
      updated.toDto()
    }

  suspend fun delete(
    userId: UUID,
    eventId: UUID
  ): Unit =
    transactor.transaction {
      val event = events.findById(eventId) ?: throw AppException(AppError.NotFound)
      access.requireAccess(userId, event.productionId, AccessLevel.WRITE)
      events.delete(eventId)
    }

  private fun parseDate(value: String): LocalDate =
    runCatching { LocalDate.parse(value) }.getOrElse {
      throw AppException(AppError.ValidationError(mapOf("date" to "Must be an ISO date (yyyy-MM-dd)")))
    }

  private fun monthRange(value: String): Pair<LocalDate, LocalDate> {
    val firstDay =
      runCatching { LocalDate.parse("$value-01") }.getOrElse {
        throw AppException(AppError.ValidationError(mapOf("date" to "Must be an ISO month (yyyy-MM)")))
      }
    val lastDay = firstDay.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
    return firstDay to lastDay
  }

  private fun generateDays(
    start: LocalDate,
    end: LocalDate
  ): List<LocalDate> {
    val days = mutableListOf<LocalDate>()
    var current = start
    while (current <= end) {
      days += current
      current = current.plus(1, DateTimeUnit.DAY)
    }
    return days
  }

  private fun ScheduleEventRecord.toDto(): ScheduleEventDto =
    ScheduleEventDto(
      id = id.toString(),
      title = title,
      location = location,
      startsAt = startsAt,
      endsAt = endsAt,
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
      dueDate = requireNotNull(dueDate) { "Schedule task must have a due date" },
      status = status,
      priority = priority
    )

  private companion object {
    // Match the column sizes in ScheduleEventsTable.
    const val MAX_TITLE_LENGTH = 200
    const val MAX_LOCATION_LENGTH = 300
  }
}
