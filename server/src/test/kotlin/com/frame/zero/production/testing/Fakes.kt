package com.frame.zero.production.testing

import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.ProductionSort
import com.frame.zero.production.ProductionMemberRecord
import com.frame.zero.production.ProductionMemberRepository
import com.frame.zero.production.ProductionRecord
import com.frame.zero.production.ProductionRepository
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
    ownerUserId: UUID
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
        updatedAt = Instant.now()
      )
    productions += record
    return record
  }

  override suspend fun findById(id: UUID): ProductionRecord? =
    productions.firstOrNull {
      it.id == id && it.deletedAt == null
    }

  override suspend fun findAccessible(
    userId: UUID,
    phases: List<ProductionPhase>,
    query: String?,
    sort: ProductionSort,
    limit: Int,
    cursor: String?
  ): Pair<List<ProductionRecord>, String?> {
    val filtered =
      productions.filter { p ->
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

  override suspend fun countActiveForUser(userId: UUID): Int =
    productions.count {
      it.deletedAt == null && it.ownerUserId == userId && it.phase != ProductionPhase.DISTRIBUTION
    }

  override suspend fun update(
    id: UUID,
    title: String?,
    logline: String?,
    startDate: LocalDate?,
    wrapDate: LocalDate?,
    budgetCents: Long?
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
        updatedAt = Instant.now()
      )
    productions[idx] = updated
    return updated
  }

  override suspend fun updatePhase(
    id: UUID,
    phase: ProductionPhase
  ): ProductionRecord? {
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

  override suspend fun findById(id: UUID): ProductionMemberRecord? =
    members.firstOrNull {
      it.id == id
    }

  override suspend fun countByProduction(productionId: UUID): Int =
    members.count {
      it.productionId == productionId
    }

  override suspend fun add(
    productionId: UUID,
    userId: UUID?,
    name: String,
    role: String,
    email: String?
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
        addedAt = Instant.now()
      )
    members += record
    return record
  }

  override suspend fun updateRole(
    id: UUID,
    role: String
  ): ProductionMemberRecord? {
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

  override suspend fun isOwner(
    userId: UUID,
    productionId: UUID
  ): Boolean =
    members.none {
      it.productionId == productionId && it.userId == userId
    }
}
