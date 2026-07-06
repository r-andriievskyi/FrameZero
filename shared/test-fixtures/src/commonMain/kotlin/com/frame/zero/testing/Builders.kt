package com.frame.zero.testing

import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionMember
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.task.TaskDetail
import com.frame.zero.domain.task.TaskParticipant
import com.frame.zero.domain.task.TaskPriority
import com.frame.zero.domain.task.TaskStatus
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** Shared domain-model builders for client tests. Override only the fields a test cares about. */

fun productionDetail(
  id: String = "p1",
  title: String = "Pilot",
  genre: Genre = Genre.DRAMA,
  startDate: LocalDate = LocalDate(2026, 4, 1),
  wrapDate: LocalDate = LocalDate(2026, 5, 1)
): ProductionDetail =
  ProductionDetail(
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
    pipeline = persistentListOf(),
    createdAt = Instant.fromEpochMilliseconds(0),
    updatedAt = Instant.fromEpochMilliseconds(0),
    viewerCrew = null
  )

fun productionMember(
  id: String = "m1",
  userId: String? = "u1",
  name: String = "Ada",
  role: String = "Director",
  initials: String = "AD",
  avatarColorHex: String? = "#FF0000"
): ProductionMember =
  ProductionMember(
    id = id,
    userId = userId,
    name = name,
    role = role,
    initials = initials,
    avatarColorHex = avatarColorHex,
    addedAt = Instant.fromEpochMilliseconds(0),
    reportsToMemberId = null
  )

fun taskDetail(
  id: String = "t1",
  productionId: String = "p1",
  title: String = "Storyboard",
  description: String? = null,
  priority: TaskPriority = TaskPriority.MEDIUM,
  participants: List<TaskParticipant> = emptyList()
): TaskDetail =
  TaskDetail(
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
    createdAt = Instant.fromEpochMilliseconds(0),
    participants = participants
  )

fun taskParticipant(
  userId: String = "u1",
  name: String = "Ada",
  avatarColorHex: String? = "#FF0000"
): TaskParticipant =
  TaskParticipant(
    userId = userId,
    name = name,
    avatarColorHex = avatarColorHex
  )
