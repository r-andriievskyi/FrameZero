package com.frame.zero.task

import com.frame.zero.auth.UsersTable
import com.frame.zero.production.ProductionsTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.timestamp

object TasksTable : Table("tasks") {
  val id = javaUUID("id")
  val productionId = javaUUID("production_id").references(ProductionsTable.id)
  val title = varchar("title", 200)
  val description = text("description").nullable()
  val dueDate = date("due_date").nullable()
  val status = varchar("status", 10)
  val priority = varchar("priority", 10).default("MEDIUM")
  val assigneeUserId = javaUUID("assignee_user_id").references(UsersTable.id).nullable()
  val createdAt = timestamp("created_at")

  val idempotencyKey = varchar("idempotency_key", 64).nullable()

  // Nullable because rows created before V5 have no recorded creator.
  val createdByUserId = javaUUID("created_by_user_id").references(UsersTable.id).nullable()

  override val primaryKey = PrimaryKey(id)

  init {
    index("idx_tasks_production", false, productionId)
    index("idx_tasks_assignee", false, assigneeUserId)
    index("idx_tasks_due_date", false, dueDate)
    index("idx_tasks_created_by", false, createdByUserId)
    uniqueIndex("tasks_idempotency_key_unique", idempotencyKey)
  }
}
