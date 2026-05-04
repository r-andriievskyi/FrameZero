package com.frame.zero.repository

import com.frame.zero.config.dbQuery
import com.frame.zero.database.ProductionMembersTable
import com.frame.zero.database.UsersTable
import java.time.Instant
import java.util.UUID
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

data class ProductionMemberRecord(
  val id: UUID,
  val productionId: UUID,
  val userId: UUID?,
  val name: String,
  val role: String,
  val email: String?,
  val avatarColorHex: String?,
  val addedAt: Instant,
)

interface ProductionMemberRepository {
  suspend fun findByProduction(productionId: UUID): List<ProductionMemberRecord>

  suspend fun findById(id: UUID): ProductionMemberRecord?

  suspend fun countByProduction(productionId: UUID): Int

  suspend fun add(
    productionId: UUID,
    userId: UUID?,
    name: String,
    role: String,
    email: String?,
  ): ProductionMemberRecord

  suspend fun updateRole(id: UUID, role: String): ProductionMemberRecord?

  suspend fun remove(id: UUID): Boolean

  suspend fun isOwner(userId: UUID, productionId: UUID): Boolean
}

class ProductionMemberRepositoryExposed : ProductionMemberRepository {
  override suspend fun findByProduction(productionId: UUID): List<ProductionMemberRecord> =
    dbQuery {
      ProductionMembersTable.leftJoin(UsersTable)
        .selectAll()
        .where { ProductionMembersTable.productionId eq productionId }
        .orderBy(ProductionMembersTable.addedAt)
        .map { it.toRecord() }
    }

  override suspend fun findById(id: UUID): ProductionMemberRecord? = dbQuery {
    ProductionMembersTable.leftJoin(UsersTable)
      .selectAll()
      .where { ProductionMembersTable.id eq id }
      .singleOrNull()
      ?.toRecord()
  }

  override suspend fun countByProduction(productionId: UUID): Int = dbQuery {
    ProductionMembersTable.selectAll()
      .where { ProductionMembersTable.productionId eq productionId }
      .count()
      .toInt()
  }

  override suspend fun add(
    productionId: UUID,
    userId: UUID?,
    name: String,
    role: String,
    email: String?,
  ): ProductionMemberRecord = dbQuery {
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
        UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()
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
    )
  }

  override suspend fun updateRole(id: UUID, role: String): ProductionMemberRecord? = dbQuery {
    val updated =
      ProductionMembersTable.update({ ProductionMembersTable.id eq id }) {
        it[ProductionMembersTable.role] = role
      }
    if (updated == 0) null
    else
      ProductionMembersTable.leftJoin(UsersTable)
        .selectAll()
        .where { ProductionMembersTable.id eq id }
        .singleOrNull()
        ?.toRecord()
  }

  override suspend fun remove(id: UUID): Boolean = dbQuery {
    ProductionMembersTable.deleteWhere { ProductionMembersTable.id eq id } > 0
  }

  override suspend fun isOwner(userId: UUID, productionId: UUID): Boolean = dbQuery {
    ProductionMembersTable.selectAll()
      .where {
        (ProductionMembersTable.productionId eq productionId) and
          (ProductionMembersTable.userId eq userId)
      }
      .count() == 0L
  }

  private fun ResultRow.toRecord(): ProductionMemberRecord =
    ProductionMemberRecord(
      id = this[ProductionMembersTable.id],
      productionId = this[ProductionMembersTable.productionId],
      userId = this[ProductionMembersTable.userId],
      name = this[ProductionMembersTable.name],
      role = this[ProductionMembersTable.role],
      email = this[ProductionMembersTable.email],
      avatarColorHex =
        this.getOrNull(UsersTable.avatarColorHex),
      addedAt = this[ProductionMembersTable.addedAt],
    )
}
