package com.frame.zero.chat

import com.frame.zero.production.ProductionsTable
import com.frame.zero.task.TasksTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object ConversationsTable : Table("conversations") {
  val id = javaUUID("id")
  val kind = varchar("kind", 20)

  val taskId = javaUUID("task_id").references(TasksTable.id, onDelete = ReferenceOption.CASCADE).nullable()

  val productionId = javaUUID("production_id").references(ProductionsTable.id)
  val createdAt = timestamp("created_at")

  override val primaryKey = PrimaryKey(id)

  init {
    uniqueIndex("conversations_task_unique", taskId)
    index("idx_conversations_production", false, productionId)
  }
}
