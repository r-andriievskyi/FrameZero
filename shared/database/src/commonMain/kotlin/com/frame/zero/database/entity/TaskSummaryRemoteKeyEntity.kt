package com.frame.zero.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_summary_remote_keys")
data class TaskSummaryRemoteKeyEntity(
  @PrimaryKey val id: Int = SINGLETON_ID,
  val nextCursor: String?
) {
  companion object {
    const val SINGLETON_ID: Int = 0
  }
}
