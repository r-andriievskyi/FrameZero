package com.frame.zero.production

import com.frame.zero.auth.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.timestamp

object ProductionsTable : Table("productions") {
  val id = javaUUID("id")
  val title = varchar("title", 120)
  val genre = varchar("genre", 20)
  val logline = varchar("logline", 280).nullable()
  val phase = varchar("phase", 20)
  val startDate = date("start_date")
  val wrapDate = date("wrap_date")
  val budgetCents = long("budget_cents").nullable()
  val ownerUserId = javaUUID("owner_user_id").references(UsersTable.id)
  val deletedAt = timestamp("deleted_at").nullable()
  val createdAt = timestamp("created_at")
  val updatedAt = timestamp("updated_at")

  override val primaryKey = PrimaryKey(id)

  init {
    index("idx_productions_owner", false, ownerUserId)
    index("idx_productions_updated_at", false, updatedAt)
  }
}
