package com.frame.zero.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.timestamp

object TasksTable : Table("tasks") {
  val id = javaUUID("id")
  val productionId = javaUUID("production_id").references(ProductionsTable.id)
  val title = varchar("title", 200)
  val description = text("description").nullable()
  val dueDate = date("due_date").nullable()
  val status = varchar("status", 10)
  val assigneeUserId = javaUUID("assignee_user_id").references(UsersTable.id).nullable()
  val createdAt = timestamp("created_at")

  override val primaryKey = PrimaryKey(id)
}
