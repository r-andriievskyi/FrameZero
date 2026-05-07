package com.frame.zero.schedule

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.toKotlin
import com.frame.zero.dto.schedule.CreateScheduleEventRequest
import com.frame.zero.dto.schedule.ScheduleDayDto
import com.frame.zero.dto.schedule.ScheduleEventDto
import com.frame.zero.dto.schedule.ScheduleItemDto
import com.frame.zero.dto.schedule.ScheduleItemSource
import com.frame.zero.dto.schedule.ScheduleResponse
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
          val date = LocalDate.parse(dateParam)
          Pair(date, date)
        }
        "week" -> {
          val date = LocalDate.parse(dateParam)
          val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
          val sunday = monday.plusDays(6)
          Pair(monday, sunday)
        }
        "month" -> {
          val ym = YearMonth.parse(dateParam)
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
        val eventItems =
          eventRecords
            .filter { it.startsAt >= dayStart && it.startsAt < dayEnd }
            .map { it.toScheduleItem() }
        val taskItems = taskRecords.filter { it.dueDate == date }.map { it.toScheduleItem() }
        ScheduleDayDto(date = date.toKotlin(), items = eventItems + taskItems)
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

    val requestTitle = request.title
    if (requestTitle != null && requestTitle.isBlank()) {
      throw AppException(AppError.ValidationError(mapOf("title" to "Cannot be empty")))
    }

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

  private fun ScheduleEventRecord.toScheduleItem(): ScheduleItemDto =
    ScheduleItemDto(
      id = id.toString(),
      source = ScheduleItemSource.EVENT,
      title = title,
      productionId = productionId.toString(),
      productionTitle = productionTitle,
      startsAt = startsAt.toKotlinInstant(),
      endsAt = endsAt.toKotlinInstant(),
      location = location,
      eventKind = kind
    )

  private fun TaskRecord.toScheduleItem(): ScheduleItemDto =
    ScheduleItemDto(
      id = id.toString(),
      source = ScheduleItemSource.TASK,
      title = title,
      productionId = productionId.toString(),
      productionTitle = productionTitle,
      dueDate = dueDate?.toKotlin(),
      taskStatus = status
    )
}
