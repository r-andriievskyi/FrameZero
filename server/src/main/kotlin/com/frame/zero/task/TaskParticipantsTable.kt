package com.frame.zero.task

import com.frame.zero.auth.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

object TaskParticipantsTable : Table("task_participants") {
  val taskId = javaUUID("task_id").references(TasksTable.id)
  val userId = javaUUID("user_id").references(UsersTable.id)

  override val primaryKey = PrimaryKey(taskId, userId)

  init {
    index("idx_task_participants_user", false, userId)
  }
}
