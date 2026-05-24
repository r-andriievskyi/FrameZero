package com.frame.zero.production

import com.frame.zero.auth.UsersTable
import com.frame.zero.config.dbQuery
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.util.UUID

data class ProductionMemberRecord(
  val id: UUID,
  val productionId: UUID,
  val userId: UUID?,
  val name: String,
  val role: String,
  val email: String?,
  val avatarColorHex: String?,
  val addedAt: Instant,
  val reportsToMemberId: UUID?
)

interface ProductionMemberRepository {
  suspend fun findByProduction(productionId: UUID): List<ProductionMemberRecord>

  suspend fun findById(id: UUID): ProductionMemberRecord?

  suspend fun countByProduction(productionId: UUID): Int

  suspend fun countByProductions(productionIds: List<UUID>): Map<UUID, Int>

  suspend fun isMember(
    userId: UUID,
    productionId: UUID
  ): Boolean

  suspend fun add(
    productionId: UUID,
    userId: UUID?,
    name: String,
    role: String,
    email: String?
  ): ProductionMemberRecord

  suspend fun updateRole(
    id: UUID,
    role: String
  ): ProductionMemberRecord?

  suspend fun updateReportsTo(
    id: UUID,
    reportsToMemberId: UUID?
  ): ProductionMemberRecord?

  suspend fun remove(id: UUID): Boolean
}

class ProductionMemberRepositoryImpl : ProductionMemberRepository {
  override suspend fun findByProduction(productionId: UUID): List<ProductionMemberRecord> =
    dbQuery {
      ProductionMembersTable
        .leftJoin(UsersTable)
        .selectAll()
        .where { ProductionMembersTable.productionId eq productionId }
        .orderBy(ProductionMembersTable.addedAt)
        .map { it.toRecord() }
    }

  override suspend fun findById(id: UUID): ProductionMemberRecord? =
    dbQuery {
      ProductionMembersTable
        .leftJoin(UsersTable)
        .selectAll()
        .where { ProductionMembersTable.id eq id }
        .singleOrNull()
        ?.toRecord()
    }

  override suspend fun countByProduction(productionId: UUID): Int =
    dbQuery {
      ProductionMembersTable
        .selectAll()
        .where { ProductionMembersTable.productionId eq productionId }
        .count()
        .toInt()
    }

  override suspend fun countByProductions(productionIds: List<UUID>): Map<UUID, Int> =
    dbQuery {
      if (productionIds.isEmpty()) return@dbQuery emptyMap()
      ProductionMembersTable
        .selectAll()
        .where { ProductionMembersTable.productionId inList productionIds }
        .groupBy { it[ProductionMembersTable.productionId] }
        .mapValues { it.value.size }
    }

  override suspend fun isMember(
    userId: UUID,
    productionId: UUID
  ): Boolean =
    dbQuery {
      ProductionMembersTable
        .selectAll()
        .where {
          (ProductionMembersTable.productionId eq productionId) and
            (ProductionMembersTable.userId eq userId)
        }.limit(1)
        .any()
    }

  override suspend fun add(
    productionId: UUID,
    userId: UUID?,
    name: String,
    role: String,
    email: String?
  ): ProductionMemberRecord =
    dbQuery {
      val newId = UUID.randomUUID()
      val now = Instant.now()
      ProductionMembersTable.insert {
        it[id] = newId
        it[ProductionMembersTable.productionId] = productionId
        it[ProductionMembersTable.userId] = userId
        it[ProductionMembersTable.name] = name
        it[ProductionMembersTable.role] = role
        it[ProductionMembersTable.email] = email
        it[addedAt] = now
      }

      val avatarColor =
        if (userId != null) {
          UsersTable
            .selectAll()
            .where { UsersTable.id eq userId }
            .singleOrNull()
            ?.get(UsersTable.avatarColorHex)
        } else {
          null
        }

      ProductionMemberRecord(
        id = newId,
        productionId = productionId,
        userId = userId,
        name = name,
        role = role,
        email = email,
        avatarColorHex = avatarColor,
        addedAt = now,
        reportsToMemberId = null
      )
    }

  override suspend fun updateRole(
    id: UUID,
    role: String
  ): ProductionMemberRecord? =
    dbQuery {
      val updated =
        ProductionMembersTable.update({ ProductionMembersTable.id eq id }) {
          it[ProductionMembersTable.role] = role
        }
      if (updated == 0) {
        null
      } else {
        ProductionMembersTable
          .leftJoin(UsersTable)
          .selectAll()
          .where { ProductionMembersTable.id eq id }
          .singleOrNull()
          ?.toRecord()
      }
    }

  override suspend fun updateReportsTo(
    id: UUID,
    reportsToMemberId: UUID?
  ): ProductionMemberRecord? =
    dbQuery {
      val updated =
        ProductionMembersTable.update({ ProductionMembersTable.id eq id }) {
          it[ProductionMembersTable.reportsToMemberId] = reportsToMemberId
        }
      if (updated == 0) {
        null
      } else {
        ProductionMembersTable
          .leftJoin(UsersTable)
          .selectAll()
          .where { ProductionMembersTable.id eq id }
          .singleOrNull()
          ?.toRecord()
      }
    }

  override suspend fun remove(id: UUID): Boolean =
    dbQuery {
      ProductionMembersTable.deleteWhere { ProductionMembersTable.id eq id } > 0
    }

  private fun ResultRow.toRecord(): ProductionMemberRecord =
    ProductionMemberRecord(
      id = this[ProductionMembersTable.id],
      productionId = this[ProductionMembersTable.productionId],
      userId = this[ProductionMembersTable.userId],
      name = this[ProductionMembersTable.name],
      role = this[ProductionMembersTable.role],
      email = this[ProductionMembersTable.email],
      avatarColorHex = this.getOrNull(UsersTable.avatarColorHex),
      addedAt = this[ProductionMembersTable.addedAt],
      reportsToMemberId = this[ProductionMembersTable.reportsToMemberId]
    )
}
