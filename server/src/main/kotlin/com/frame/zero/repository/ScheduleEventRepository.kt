package com.frame.zero.repository

import com.frame.zero.config.dbQuery
import com.frame.zero.database.ProductionMembersTable
import com.frame.zero.database.ProductionsTable
import com.frame.zero.database.ScheduleEventsTable
import com.frame.zero.domain.schedule.ScheduleEventKind
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.util.UUID

data class ScheduleEventRecord(
  val id: UUID,
  val productionId: UUID,
  val productionTitle: String,
  val title: String,
  val location: String?,
  val startsAt: Instant,
  val endsAt: Instant,
  val kind: ScheduleEventKind
)

interface ScheduleEventRepository {
  suspend fun findInRangeForUser(
    userId: UUID,
    rangeStart: Instant,
    rangeEnd: Instant
  ): List<ScheduleEventRecord>

  suspend fun findById(id: UUID): ScheduleEventRecord?

  suspend fun create(
    productionId: UUID,
    title: String,
    location: String?,
    startsAt: Instant,
    endsAt: Instant,
    kind: ScheduleEventKind
  ): ScheduleEventRecord

  suspend fun update(
    id: UUID,
    title: String?,
    location: String?,
    startsAt: Instant?,
    endsAt: Instant?,
    kind: ScheduleEventKind?
  ): ScheduleEventRecord?

  suspend fun delete(id: UUID): Boolean
}

class ScheduleEventRepositoryExposed : ScheduleEventRepository {
  override suspend fun findInRangeForUser(
    userId: UUID,
    rangeStart: Instant,
    rangeEnd: Instant
  ): List<ScheduleEventRecord> =
    dbQuery {
      val memberProductionIds =
        ProductionMembersTable
          .selectAll()
          .where { ProductionMembersTable.userId eq userId }
          .map { it[ProductionMembersTable.productionId] }
      if (memberProductionIds.isEmpty()) return@dbQuery emptyList()

      (ScheduleEventsTable innerJoin ProductionsTable)
        .selectAll()
        .where {
          (ScheduleEventsTable.startsAt greaterEq rangeStart) and
            (ScheduleEventsTable.startsAt less rangeEnd) and
            ProductionsTable.deletedAt.isNull() and
            (ScheduleEventsTable.productionId inList memberProductionIds)
        }.orderBy(ScheduleEventsTable.startsAt to SortOrder.ASC)
        .map { it.toRecord() }
    }

  override suspend fun findById(id: UUID): ScheduleEventRecord? =
    dbQuery {
      (ScheduleEventsTable innerJoin ProductionsTable)
        .selectAll()
        .where { ScheduleEventsTable.id eq id }
        .singleOrNull()
        ?.toRecord()
    }

  override suspend fun create(
    productionId: UUID,
    title: String,
    location: String?,
    startsAt: Instant,
    endsAt: Instant,
    kind: ScheduleEventKind
  ): ScheduleEventRecord =
    dbQuery {
      val newId = UUID.randomUUID()
      ScheduleEventsTable.insert {
        it[id] = newId
        it[ScheduleEventsTable.productionId] = productionId
        it[ScheduleEventsTable.title] = title
        it[ScheduleEventsTable.location] = location
        it[ScheduleEventsTable.startsAt] = startsAt
        it[ScheduleEventsTable.endsAt] = endsAt
        it[ScheduleEventsTable.kind] = kind.name
      }
      val prodTitle =
        ProductionsTable
          .selectAll()
          .where { ProductionsTable.id eq productionId }
          .singleOrNull()
          ?.get(ProductionsTable.title) ?: ""
      ScheduleEventRecord(
        id = newId,
        productionId = productionId,
        productionTitle = prodTitle,
        title = title,
        location = location,
        startsAt = startsAt,
        endsAt = endsAt,
        kind = kind
      )
    }

  override suspend fun update(
    id: UUID,
    title: String?,
    location: String?,
    startsAt: Instant?,
    endsAt: Instant?,
    kind: ScheduleEventKind?
  ): ScheduleEventRecord? =
    dbQuery {
      val updated =
        ScheduleEventsTable.update({ ScheduleEventsTable.id eq id }) { row ->
          title?.let { row[ScheduleEventsTable.title] = it }
          location?.let { row[ScheduleEventsTable.location] = it }
          startsAt?.let { row[ScheduleEventsTable.startsAt] = it }
          endsAt?.let { row[ScheduleEventsTable.endsAt] = it }
          kind?.let { row[ScheduleEventsTable.kind] = it.name }
        }
      if (updated == 0) {
        null
      } else {
        (ScheduleEventsTable innerJoin ProductionsTable)
          .selectAll()
          .where { ScheduleEventsTable.id eq id }
          .singleOrNull()
          ?.toRecord()
      }
    }

  override suspend fun delete(id: UUID): Boolean =
    dbQuery {
      ScheduleEventsTable.deleteWhere { ScheduleEventsTable.id eq id } > 0
    }

  private fun ResultRow.toRecord(): ScheduleEventRecord =
    ScheduleEventRecord(
      id = this[ScheduleEventsTable.id],
      productionId = this[ScheduleEventsTable.productionId],
      productionTitle = this[ProductionsTable.title],
      title = this[ScheduleEventsTable.title],
      location = this[ScheduleEventsTable.location],
      startsAt = this[ScheduleEventsTable.startsAt],
      endsAt = this[ScheduleEventsTable.endsAt],
      kind = ScheduleEventKind.valueOf(this[ScheduleEventsTable.kind])
    )
}
