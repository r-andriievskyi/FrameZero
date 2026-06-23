package com.frame.zero.testing

import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionMemberDto
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.dto.task.TaskStatus
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** Shared DTO builders for client tests. Override only the fields a test cares about. */

fun productionDetailDto(
  id: String = "p1",
  title: String = "Pilot",
  genre: Genre = Genre.DRAMA,
  startDate: LocalDate = LocalDate(2026, 4, 1),
  wrapDate: LocalDate = LocalDate(2026, 5, 1)
): ProductionDetailDto =
  ProductionDetailDto(
    id = id,
    title = title,
    genre = genre,
    logline = null,
    phase = ProductionPhase.IDEA,
    progressPercent = 0,
    daysLeft = 0,
    startDate = startDate,
    wrapDate = wrapDate,
    budgetCents = null,
    membersCount = 0,
    keyCrew = emptyList(),
    pipeline = emptyList(),
    createdAt = Instant.fromEpochMilliseconds(0),
    updatedAt = Instant.fromEpochMilliseconds(0)
  )

fun productionMemberDto(
  id: String = "m1",
  userId: String? = "u1",
  name: String = "Ada",
  role: String = "Director",
  initials: String = "AD",
  avatarColorHex: String? = "#FF0000"
): ProductionMemberDto =
  ProductionMemberDto(
    id = id,
    userId = userId,
    name = name,
    role = role,
    initials = initials,
    avatarColorHex = avatarColorHex,
    addedAt = Instant.fromEpochMilliseconds(0)
  )

fun taskDetailDto(
  id: String = "t1",
  productionId: String = "p1",
  title: String = "Storyboard",
  description: String? = null,
  priority: TaskPriority = TaskPriority.MEDIUM
): TaskDetailDto =
  TaskDetailDto(
    id = id,
    productionId = productionId,
    productionTitle = "Pilot",
    title = title,
    description = description,
    dueDate = null,
    status = TaskStatus.OPEN,
    priority = priority,
    assigneeUserId = null,
    assignee = null,
    createdAt = Instant.fromEpochMilliseconds(0)
  )
