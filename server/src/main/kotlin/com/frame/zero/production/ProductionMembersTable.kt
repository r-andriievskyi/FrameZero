package com.frame.zero.production

import com.frame.zero.auth.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestamp

object ProductionMembersTable : Table("production_members") {
  val id = javaUUID("id")
  val productionId = javaUUID("production_id").references(ProductionsTable.id)
  val userId = javaUUID("user_id").references(UsersTable.id).nullable()
  val name = varchar("name", 200)
  val role = varchar("role", 100)
  val email = varchar("email", 320).nullable()
  val addedAt = timestamp("added_at")
  val reportsToMemberId = javaUUID("reports_to_member_id").nullable()

  override val primaryKey = PrimaryKey(id)

  init {
    index("idx_members_production", false, productionId)
    index("idx_members_user", false, userId)
    index("idx_members_reports_to", false, reportsToMemberId)
  }
}
