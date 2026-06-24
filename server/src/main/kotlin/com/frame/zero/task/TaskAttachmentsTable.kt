package com.frame.zero.task

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object TaskAttachmentsTable : Table("task_attachments") {
  val id = javaUUID("id")
  val taskId = javaUUID("task_id").references(TasksTable.id)
  val fileName = varchar("file_name", 255)
  val contentType = varchar("content_type", 127)
  val sizeBytes = long("size_bytes")
  val storageKey = varchar("storage_key", 255)
  val createdAt = timestamp("created_at")

  override val primaryKey = PrimaryKey(id)

  init {
    uniqueIndex("task_attachments_task_unique", taskId)
  }
}
