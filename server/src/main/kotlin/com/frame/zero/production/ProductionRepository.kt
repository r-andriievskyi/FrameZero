package com.frame.zero.production

import com.frame.zero.common.decodeCursor
import com.frame.zero.common.encodeCursor
import com.frame.zero.config.dbQuery
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.ProductionSort
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.exists
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class ProductionRecord(
  val id: UUID,
  val title: String,
  val genre: Genre,
  val logline: String?,
  val phase: ProductionPhase,
  val startDate: LocalDate,
  val wrapDate: LocalDate,
  val budgetCents: Long?,
  val ownerUserId: UUID,
  val deletedAt: Instant?,
  val createdAt: Instant,
  val updatedAt: Instant
)

interface ProductionRepository {
  suspend fun create(
    title: String,
    genre: Genre,
    logline: String?,
    phase: ProductionPhase,
    startDate: LocalDate,
    wrapDate: LocalDate,
    budgetCents: Long?,
    ownerUserId: UUID
  ): ProductionRecord

  suspend fun findById(id: UUID): ProductionRecord?

  suspend fun findAccessible(
    userId: UUID,
    phases: List<ProductionPhase>,
    query: String?,
    sort: ProductionSort,
    limit: Int,
    cursor: String?
  ): Pair<List<ProductionRecord>, String?>

  suspend fun countActiveForUser(userId: UUID): Int

  suspend fun update(
    id: UUID,
    title: String?,
    logline: String?,
    startDate: LocalDate?,
    wrapDate: LocalDate?,
    budgetCents: Long?
  ): ProductionRecord?

  suspend fun updatePhase(
    id: UUID,
    phase: ProductionPhase
  ): ProductionRecord?

  suspend fun softDelete(id: UUID)
}

class ProductionRepositoryImpl : ProductionRepository {
  override suspend fun create(
    title: String,
    genre: Genre,
    logline: String?,
    phase: ProductionPhase,
    startDate: LocalDate,
    wrapDate: LocalDate,
    budgetCents: Long?,
    ownerUserId: UUID
  ): ProductionRecord =
    dbQuery {
      val newId = UUID.randomUUID()
      val now = Instant.now()
      ProductionsTable.insert {
        it[id] = newId
        it[ProductionsTable.title] = title
        it[ProductionsTable.genre] = genre.name
        it[ProductionsTable.logline] = logline
        it[ProductionsTable.phase] = phase.name
        it[ProductionsTable.startDate] = startDate
        it[ProductionsTable.wrapDate] = wrapDate
        it[ProductionsTable.budgetCents] = budgetCents
        it[ProductionsTable.ownerUserId] = ownerUserId
        it[createdAt] = now
        it[updatedAt] = now
      }
      ProductionRecord(
        id = newId,
        title = title,
        genre = genre,
        logline = logline,
        phase = phase,
        startDate = startDate,
        wrapDate = wrapDate,
        budgetCents = budgetCents,
        ownerUserId = ownerUserId,
        deletedAt = null,
        createdAt = now,
        updatedAt = now
      )
    }

  override suspend fun findById(id: UUID): ProductionRecord? =
    dbQuery {
      ProductionsTable
        .selectAll()
        .where { (ProductionsTable.id eq id) and ProductionsTable.deletedAt.isNull() }
        .singleOrNull()
        ?.toRecord()
    }

  override suspend fun findAccessible(
    userId: UUID,
    phases: List<ProductionPhase>,
    query: String?,
    sort: ProductionSort,
    limit: Int,
    cursor: String?
  ): Pair<List<ProductionRecord>, String?> =
    dbQuery {
      val baseQuery =
        ProductionsTable.selectAll().where {
          // A correlated EXISTS against the membership table avoids materializing
          // every member-production id into an IN (...) list.
          val accessCond =
            (ProductionsTable.ownerUserId eq userId) or
              exists(
                ProductionMembersTable.selectAll().where {
                  (ProductionMembersTable.userId eq userId) and
                    (ProductionMembersTable.productionId eq ProductionsTable.id)
                }
              )

          var cond = ProductionsTable.deletedAt.isNull() and accessCond

          if (phases.isNotEmpty()) {
            val phaseNames = phases.map { it.name }
            cond = cond and (ProductionsTable.phase inList phaseNames)
          }

          if (!query.isNullOrBlank()) {
            val escaped = query.lowercase()
              .replace("\\", "\\\\")
              .replace("%", "\\%")
              .replace("_", "\\_")
            cond = cond and (ProductionsTable.title.lowerCase() like "%$escaped%")
          }

          if (cursor != null) {
            val pc = decodeCursor(cursor)
            if (pc != null) {
              cond =
                cond and
                when (sort) {
                  ProductionSort.DUE_DATE -> {
                    val cursorDate = LocalDate.ofEpochDay(pc.epochMillis)
                    (ProductionsTable.wrapDate greater cursorDate) or
                      (
                        (ProductionsTable.wrapDate eq cursorDate) and
                          (ProductionsTable.id greater pc.id)
                      )
                  }
                  ProductionSort.RECENT -> {
                    val cursorTs = Instant.ofEpochMilli(pc.epochMillis)
                    (ProductionsTable.updatedAt less cursorTs) or
                      (
                        (ProductionsTable.updatedAt eq cursorTs) and
                          (ProductionsTable.id less pc.id)
                      )
                  }
                }
            }
          }

          cond
        }

      val ordered =
        when (sort) {
          ProductionSort.DUE_DATE ->
            baseQuery.orderBy(
              ProductionsTable.wrapDate to SortOrder.ASC,
              ProductionsTable.id to SortOrder.ASC
            )
          ProductionSort.RECENT ->
            baseQuery.orderBy(
              ProductionsTable.updatedAt to SortOrder.DESC,
              ProductionsTable.id to SortOrder.DESC
            )
        }

      val rows = ordered.limit(limit + 1).map { it.toRecord() }

      val hasMore = rows.size > limit
      val items = if (hasMore) rows.dropLast(1) else rows
      val nextCursor =
        if (hasMore) {
          val last = items.last()
          when (sort) {
            ProductionSort.DUE_DATE -> encodeCursor(last.wrapDate.toEpochDay(), last.id)
            ProductionSort.RECENT -> encodeCursor(last.updatedAt.toEpochMilli(), last.id)
          }
        } else {
          null
        }
      Pair(items, nextCursor)
    }

  override suspend fun countActiveForUser(userId: UUID): Int =
    dbQuery {
      val accessCond =
        (ProductionsTable.ownerUserId eq userId) or
          exists(
            ProductionMembersTable.selectAll().where {
              (ProductionMembersTable.userId eq userId) and
                (ProductionMembersTable.productionId eq ProductionsTable.id)
            }
          )
      ProductionsTable
        .selectAll()
        .where {
          ProductionsTable.deletedAt.isNull() and
            (ProductionsTable.phase neq ProductionPhase.DISTRIBUTION.name) and
            (ProductionsTable.phase neq ProductionPhase.ARCHIVED.name) and
            accessCond
        }.count()
        .toInt()
    }

  override suspend fun update(
    id: UUID,
    title: String?,
    logline: String?,
    startDate: LocalDate?,
    wrapDate: LocalDate?,
    budgetCents: Long?
  ): ProductionRecord? =
    dbQuery {
      val now = Instant.now()
      val updated =
        ProductionsTable.update({ ProductionsTable.id eq id }) { row ->
          title?.let { row[ProductionsTable.title] = it }
          logline?.let { row[ProductionsTable.logline] = it }
          startDate?.let { row[ProductionsTable.startDate] = it }
          wrapDate?.let { row[ProductionsTable.wrapDate] = it }
          budgetCents?.let { row[ProductionsTable.budgetCents] = it }
          row[updatedAt] = now
        }
      if (updated == 0) {
        null
      } else {
        ProductionsTable
          .selectAll()
          .where { ProductionsTable.id eq id }
          .singleOrNull()
          ?.toRecord()
      }
    }

  override suspend fun updatePhase(
    id: UUID,
    phase: ProductionPhase
  ): ProductionRecord? =
    dbQuery {
      val now = Instant.now()
      ProductionsTable.update({ ProductionsTable.id eq id }) {
        it[ProductionsTable.phase] = phase.name
        it[updatedAt] = now
      }
      ProductionsTable
        .selectAll()
        .where { ProductionsTable.id eq id }
        .singleOrNull()
        ?.toRecord()
    }

  override suspend fun softDelete(id: UUID): Unit =
    dbQuery {
      ProductionsTable.update({ ProductionsTable.id eq id }) { it[deletedAt] = Instant.now() }
    }

  private fun ResultRow.toRecord(): ProductionRecord =
    ProductionRecord(
      id = this[ProductionsTable.id],
      title = this[ProductionsTable.title],
      genre = Genre.valueOf(this[ProductionsTable.genre]),
      logline = this[ProductionsTable.logline],
      phase = ProductionPhase.valueOf(this[ProductionsTable.phase]),
      startDate = this[ProductionsTable.startDate],
      wrapDate = this[ProductionsTable.wrapDate],
      budgetCents = this[ProductionsTable.budgetCents],
      ownerUserId = this[ProductionsTable.ownerUserId],
      deletedAt = this[ProductionsTable.deletedAt],
      createdAt = this[ProductionsTable.createdAt],
      updatedAt = this[ProductionsTable.updatedAt]
    )
}
