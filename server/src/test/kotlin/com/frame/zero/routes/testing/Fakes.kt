package com.frame.zero.routes.testing

import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.ProductionSort
import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.repository.NotificationRecord
import com.frame.zero.repository.NotificationRepository
import com.frame.zero.repository.ProductionMemberRecord
import com.frame.zero.repository.ProductionMemberRepository
import com.frame.zero.repository.ProductionRecord
import com.frame.zero.repository.ProductionRepository
import com.frame.zero.repository.ScheduleEventRecord
import com.frame.zero.repository.ScheduleEventRepository
import com.frame.zero.repository.TaskRecord
import com.frame.zero.repository.TaskRepository
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

internal class FakeProductionRepository : ProductionRepository {
  val productions: MutableList<ProductionRecord> = mutableListOf()

  override suspend fun create(
    title: String,
    genre: Genre,
    logline: String?,
    phase: ProductionPhase,
    startDate: LocalDate,
    wrapDate: LocalDate,
    budgetCents: Long?,
    ownerUserId: UUID,
  ): ProductionRecord {
    val record =
      ProductionRecord(
        id = UUID.randomUUID(),
        title = title,
        genre = genre,
        logline = logline,
        phase = phase,
        startDate = startDate,
        wrapDate = wrapDate,
        budgetCents = budgetCents,
        ownerUserId = ownerUserId,
        deletedAt = null,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
      )
    productions += record
    return record
  }

  override suspend fun findById(id: UUID): ProductionRecord? = productions.firstOrNull {
    it.id == id && it.deletedAt == null
  }

  override suspend fun findAccessible(
    userId: UUID,
    phases: List<ProductionPhase>,
    query: String?,
    sort: ProductionSort,
    limit: Int,
    cursor: String?,
  ): Pair<List<ProductionRecord>, String?> {
    val filtered = productions.filter { p ->
      p.deletedAt == null &&
        (p.ownerUserId == userId) &&
        (phases.isEmpty() || p.phase in phases) &&
        (query.isNullOrBlank() || p.title.contains(query, ignoreCase = true))
    }
    val sorted =
      when (sort) {
        ProductionSort.DUE_DATE -> filtered.sortedWith(compareBy({ it.wrapDate }, { it.id }))
        ProductionSort.RECENT ->
          filtered.sortedWith(
            compareByDescending<ProductionRecord> { it.updatedAt }.thenByDescending { it.id }
          )
      }
    return Pair(sorted.take(limit), null)
  }

  override suspend fun countActiveForUser(userId: UUID): Int = productions.count {
    it.deletedAt == null && it.ownerUserId == userId && it.phase != ProductionPhase.DISTRIBUTION
  }

  override suspend fun update(
    id: UUID,
    title: String?,
    logline: String?,
    startDate: LocalDate?,
    wrapDate: LocalDate?,
    budgetCents: Long?,
  ): ProductionRecord? {
    val idx = productions.indexOfFirst { it.id == id }
    if (idx < 0) return null
    val updated =
      productions[idx].copy(
        title = title ?: productions[idx].title,
        logline = logline ?: productions[idx].logline,
        startDate = startDate ?: productions[idx].startDate,
        wrapDate = wrapDate ?: productions[idx].wrapDate,
        budgetCents = budgetCents ?: productions[idx].budgetCents,
        updatedAt = Instant.now(),
      )
    productions[idx] = updated
    return updated
  }

  override suspend fun updatePhase(id: UUID, phase: ProductionPhase): ProductionRecord? {
    val idx = productions.indexOfFirst { it.id == id }
    if (idx < 0) return null
    val updated = productions[idx].copy(phase = phase, updatedAt = Instant.now())
    productions[idx] = updated
    return updated
  }

  override suspend fun softDelete(id: UUID) {
    val idx = productions.indexOfFirst { it.id == id }
    if (idx >= 0) productions[idx] = productions[idx].copy(deletedAt = Instant.now())
  }
}

internal class FakeProductionMemberRepository : ProductionMemberRepository {
  val members: MutableList<ProductionMemberRecord> = mutableListOf()

  override suspend fun findByProduction(productionId: UUID): List<ProductionMemberRecord> =
    members.filter {
      it.productionId == productionId
    }

  override suspend fun findById(id: UUID): ProductionMemberRecord? = members.firstOrNull {
    it.id == id
  }

  override suspend fun countByProduction(productionId: UUID): Int = members.count {
    it.productionId == productionId
  }

  override suspend fun add(
    productionId: UUID,
    userId: UUID?,
    name: String,
    role: String,
    email: String?,
  ): ProductionMemberRecord {
    val record =
      ProductionMemberRecord(
        id = UUID.randomUUID(),
        productionId = productionId,
        userId = userId,
        name = name,
        role = role,
        email = email,
        avatarColorHex = null,
        addedAt = Instant.now(),
      )
    members += record
    return record
  }

  override suspend fun updateRole(id: UUID, role: String): ProductionMemberRecord? {
    val idx = members.indexOfFirst { it.id == id }
    if (idx < 0) return null
    val updated = members[idx].copy(role = role)
    members[idx] = updated
    return updated
  }

  override suspend fun remove(id: UUID): Boolean {
    val idx = members.indexOfFirst { it.id == id }
    if (idx < 0) return false
    members.removeAt(idx)
    return true
  }

  override suspend fun isOwner(userId: UUID, productionId: UUID): Boolean = members.none {
    it.productionId == productionId && it.userId == userId
  }
}

internal class FakeTaskRepository : TaskRepository {
  val tasks: MutableList<TaskRecord> = mutableListOf()

  override suspend fun create(
    productionId: UUID,
    title: String,
    description: String?,
    dueDate: LocalDate?,
    assigneeUserId: UUID?,
  ): TaskRecord {
    val record =
      TaskRecord(
        id = UUID.randomUUID(),
        productionId = productionId,
        productionTitle = "Test Production",
        title = title,
        description = description,
        dueDate = dueDate,
        status = TaskStatus.OPEN,
        assigneeUserId = assigneeUserId,
        createdAt = Instant.now(),
      )
    tasks += record
    return record
  }

  override suspend fun findById(id: UUID): TaskRecord? = tasks.firstOrNull { it.id == id }

  override suspend fun findForUser(
    userId: UUID,
    assigneeMe: Boolean,
    status: TaskStatus?,
    productionId: UUID?,
    limit: Int,
    cursor: String?,
  ): Pair<List<TaskRecord>, String?> {
    val items =
      tasks
        .filter { t ->
          (!assigneeMe || t.assigneeUserId == userId) &&
            (status == null || t.status == status) &&
            (productionId == null || t.productionId == productionId)
        }
        .take(limit)
    return Pair(items, null)
  }

  override suspend fun findForUserLimit(userId: UUID, limit: Int): List<TaskRecord> =
    tasks.filter { it.assigneeUserId == userId && it.status == TaskStatus.OPEN }.take(limit)

  override suspend fun countOpenForUser(userId: UUID): Int = tasks.count {
    it.assigneeUserId == userId && it.status == TaskStatus.OPEN
  }

  override suspend fun update(
    id: UUID,
    title: String?,
    description: String?,
    dueDate: LocalDate?,
    status: TaskStatus?,
    assigneeUserId: UUID?,
  ): TaskRecord? {
    val idx = tasks.indexOfFirst { it.id == id }
    if (idx < 0) return null
    val updated =
      tasks[idx].copy(
        title = title ?: tasks[idx].title,
        description = description ?: tasks[idx].description,
        dueDate = dueDate ?: tasks[idx].dueDate,
        status = status ?: tasks[idx].status,
        assigneeUserId = assigneeUserId ?: tasks[idx].assigneeUserId,
      )
    tasks[idx] = updated
    return updated
  }

  override suspend fun delete(id: UUID): Boolean {
    val idx = tasks.indexOfFirst { it.id == id }
    if (idx < 0) return false
    tasks.removeAt(idx)
    return true
  }
}

internal class FakeScheduleEventRepository : ScheduleEventRepository {
  val events: MutableList<ScheduleEventRecord> = mutableListOf()

  override suspend fun findInRange(
    userId: UUID,
    rangeStart: Instant,
    rangeEnd: Instant,
    productionId: UUID?,
  ): List<ScheduleEventRecord> = events.filter { e ->
    e.startsAt >= rangeStart &&
      e.startsAt < rangeEnd &&
      (productionId == null || e.productionId == productionId)
  }

  override suspend fun findById(id: UUID): ScheduleEventRecord? = events.firstOrNull { it.id == id }

  override suspend fun create(
    productionId: UUID,
    title: String,
    location: String?,
    startsAt: Instant,
    endsAt: Instant,
    kind: ScheduleEventKind,
  ): ScheduleEventRecord {
    val record =
      ScheduleEventRecord(
        id = UUID.randomUUID(),
        productionId = productionId,
        productionTitle = "Test Production",
        title = title,
        location = location,
        startsAt = startsAt,
        endsAt = endsAt,
        kind = kind,
      )
    events += record
    return record
  }

  override suspend fun update(
    id: UUID,
    title: String?,
    location: String?,
    startsAt: Instant?,
    endsAt: Instant?,
    kind: ScheduleEventKind?,
  ): ScheduleEventRecord? {
    val idx = events.indexOfFirst { it.id == id }
    if (idx < 0) return null
    val updated =
      events[idx].copy(
        title = title ?: events[idx].title,
        location = location ?: events[idx].location,
        startsAt = startsAt ?: events[idx].startsAt,
        endsAt = endsAt ?: events[idx].endsAt,
        kind = kind ?: events[idx].kind,
      )
    events[idx] = updated
    return updated
  }

  override suspend fun delete(id: UUID): Boolean {
    val idx = events.indexOfFirst { it.id == id }
    if (idx < 0) return false
    events.removeAt(idx)
    return true
  }
}

internal class FakeNotificationRepository : NotificationRepository {
  val notifications: MutableList<NotificationRecord> = mutableListOf()

  override suspend fun findForUser(
    userId: UUID,
    limit: Int,
    cursor: String?,
  ): Pair<List<NotificationRecord>, String?> {
    val items = notifications.filter { it.userId == userId }.take(limit)
    return Pair(items, null)
  }

  override suspend fun countUnread(userId: UUID): Int = notifications.count {
    it.userId == userId && it.readAt == null
  }

  override suspend fun markRead(userId: UUID, ids: List<UUID>) {
    val now = Instant.now()
    ids.forEach { id ->
      val idx = notifications.indexOfFirst { it.id == id && it.userId == userId }
      if (idx >= 0) notifications[idx] = notifications[idx].copy(readAt = now)
    }
  }

  override suspend fun markAllRead(userId: UUID) {
    val now = Instant.now()
    notifications.replaceAll { n ->
      if (n.userId == userId && n.readAt == null) n.copy(readAt = now) else n
    }
  }

  override suspend fun create(userId: UUID, title: String, body: String?): NotificationRecord {
    val record =
      NotificationRecord(
        id = UUID.randomUUID(),
        userId = userId,
        title = title,
        body = body,
        readAt = null,
        createdAt = Instant.now(),
      )
    notifications += record
    return record
  }
}
